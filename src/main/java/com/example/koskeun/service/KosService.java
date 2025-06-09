package com.example.koskeun.service;

import com.example.koskeun.dto.request.KosApprovalRequest;
import com.example.koskeun.dto.request.KosRequest;
import com.example.koskeun.model.Kos;
import com.example.koskeun.model.KosImage;
import com.example.koskeun.repository.KosImageRepository;
import com.example.koskeun.repository.KosRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KosService {

    @Autowired
    private KosRepository kosRepository;

    @Autowired
    private KosImageRepository kosImageRepository;

    @Autowired
    private AuthService authService;

    private final Path imageStorageLocation = Paths.get("uploads/kos-images");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");

    public KosService() {
        try {
            Files.createDirectories(imageStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Transactional
    public Kos createKos(Kos kos, List<MultipartFile> images) {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User must be logged in to create a kos");
        }

        validateKosData(kos);

        kos.setOwnerId(currentUser.getId());
        kos.setStatus("available");
        kos.setApprovalStatus("pending"); // Default status

        // Save the kos first to get the ID
        Kos savedKos = kosRepository.save(kos);

        // Process and save images if any
        if (images != null && !images.isEmpty()) {
            validateImages(images);
            // Create a default list of types (e.g., all "main" or "additional")
            List<String> types = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                types.add(i == 0 ? "main" : "additional"); // First image is "main", others "additional"
            }
            saveKosImages(savedKos, images, types);
        }

        return savedKos;
    }

    @Transactional
    public Kos updateKos(Long kosId, Kos kos, List<MultipartFile> newImages) {
        var existingKos = kosRepository.findById(kosId)
                .orElseThrow(() -> new RuntimeException("Kos not found"));

        var currentUser = authService.getCurrentUser();
        if (currentUser == null || !existingKos.getOwnerId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to update this kos");
        }

        validateKosData(kos);

        // Update fields
        existingKos.setName(kos.getName());
        existingKos.setPriceMonthly(kos.getPriceMonthly());
        existingKos.setDescription(kos.getDescription());
        existingKos.setType(kos.getType());
        existingKos.setCategory(kos.getCategory());
        existingKos.setAddress(kos.getAddress());
        existingKos.setLatitude(kos.getLatitude());
        existingKos.setLongitude(kos.getLongitude());
        existingKos.setStatus(kos.getStatus());

        // Save updated kos
        Kos updatedKos = kosRepository.save(existingKos);

        // Process and save new images if any
        if (newImages != null && !newImages.isEmpty()) {
            validateImages(newImages);
            // Create a default list of types for new images
            List<String> types = new ArrayList<>();
            for (int i = 0; i < newImages.size(); i++) {
                types.add("additional"); // All new images are "additional"
            }
            saveKosImages(updatedKos, newImages, types);
        }

        return updatedKos;
    }

    @Transactional
    public void deleteKos(Long kosId) {
        var kos = kosRepository.findById(kosId)
                .orElseThrow(() -> new RuntimeException("Kos not found"));

        var currentUser = authService.getCurrentUser();
        if (currentUser == null || !kos.getOwnerId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to delete this kos");
        }

        // Delete all associated images first
        var images = kosImageRepository.findByKosId(kosId);
        for (KosImage image : images) {
            deleteImageFile(image.getUrl());
        }
        kosImageRepository.deleteAll(images);

        // Delete the kos
        kosRepository.delete(kos);
    }

    @Transactional
    public void deleteKosImage(Long kosId, Long imageId) {
        var image = kosImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        if (!image.getKosId().equals(kosId)) {
            throw new RuntimeException("Image does not belong to the specified kos");
        }

        var kos = kosRepository.findById(kosId)
                .orElseThrow(() -> new RuntimeException("Kos not found"));

        var currentUser = authService.getCurrentUser();
        if (currentUser == null || !kos.getOwnerId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to delete this image");
        }

        deleteImageFile(image.getUrl());
        kosImageRepository.delete(image);
    }

    @Transactional
    public Kos approvalKos(Long kosId, KosApprovalRequest request) {
        // 1. Otorisasi: Pastikan user adalah ADMIN (atau role lain yang berwenang)
        var currentUser = authService.getCurrentUser(); // Menggunakan UserDetailsImpl dari AuthService Anda
        if (currentUser == null || !currentUser.getRoleName().equalsIgnoreCase("admin")) {
            throw new SecurityException("Hanya ADMIN yang dapat menyetujui Kos.");
        }

        // 2. Validasi input
        if (!List.of("approved", "rejected").contains(request.getApprovalStatus().toLowerCase())) {
            throw new IllegalArgumentException("Status persetujuan tidak valid. Gunakan 'approved' atau 'rejected'.");
        }

        // 3. Ambil data Kos dari database
        Kos kosToApprove = kosRepository.findById(kosId)
                .orElseThrow(() -> new EntityNotFoundException("Kos dengan ID " + kosId + " tidak ditemukan."));

        // 4. Update field-field yang relevan
        kosToApprove.setApprovalStatus(request.getApprovalStatus());
        kosToApprove.setApprovalNotes(request.getApprovalNotes());
        kosToApprove.setReviewedAt(new java.util.Date()); // Set waktu sekarang
        kosToApprove.setAdminId(currentUser.getId()); // Set admin yang melakukan approval

        // 5. Simpan perubahan ke database
        return kosRepository.save(kosToApprove);
    }

    @Transactional
    public void saveKosImages(Kos kos, List<MultipartFile> images, List<String> types) {
        if (images == null || images.isEmpty())
            return;

        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            String type = i < types.size() ? types.get(i) : "additional";

            if (!image.isEmpty()) {
                String newFilename = null;
                try {
                    // Generate unique filename
                    String originalFilename = image.getOriginalFilename();
                    if (originalFilename == null) {
                        throw new RuntimeException("Invalid file name");
                    }

                    String extension = getFileExtension(originalFilename);
                    validateFileExtension(extension);
                    newFilename = UUID.randomUUID().toString() + extension;

                    // Save file to disk
                    Path targetLocation = imageStorageLocation.resolve(newFilename);
                    Files.copy(image.getInputStream(), targetLocation);

                    // Create and save image record
                    KosImage kosImage = new KosImage();
                    kosImage.setKosId(kos.getId());
                    kosImage.setUrl(newFilename);
                    kosImage.setType(type); // Set the specific type

                    kosImageRepository.save(kosImage);
                } catch (Exception e) {
                    // Clean up the file if it was created
                    if (newFilename != null) {
                        deleteImageFile(newFilename);
                    }
                    throw new RuntimeException("Failed to process image: " + e.getMessage());
                }
            }
        }
    }

    @Transactional
    public Kos updateKosAndImages(Long kosId, KosRequest kosDetails, Map<String, MultipartFile> singleImages,
            MultipartFile[] additionalImages) {
        // 1. Ambil data Kos yang ada & validasi kepemilikan
        Kos existingKos = kosRepository.findById(kosId)
                .orElseThrow(() -> new EntityNotFoundException("Kos tidak ditemukan"));

        var currentUser = authService.getCurrentUserEntity();
        if (currentUser == null || !existingKos.getOwnerId().equals(currentUser.getId())) {
            throw new SecurityException("Anda tidak berhak memperbarui kos ini.");
        }

        // 2. Update data teks dari DTO
        existingKos.setName(kosDetails.getName());
        existingKos.setPriceMonthly(kosDetails.getPriceMonthly());
        existingKos.setDescription(kosDetails.getDescription());
        existingKos.setType(kosDetails.getType());
        existingKos.setCategory(kosDetails.getCategory());
        existingKos.setAddress(kosDetails.getAddress());
        existingKos.setLatitude(kosDetails.getLatitude());
        existingKos.setLongitude(kosDetails.getLongitude());
        existingKos.setStatus(kosDetails.getStatus());

        // 3. Proses gambar tunggal (jika ada file baru yang diupload)
        singleImages.forEach((type, file) -> {
            if (file != null && !file.isEmpty()) {
                // Hapus gambar lama dengan tipe yang sama
                kosImageRepository.findByKosIdAndType(kosId, type).forEach(oldImage -> {
                    deleteImageFile(oldImage.getUrl());
                    kosImageRepository.delete(oldImage);
                });

                // Simpan gambar baru
                saveSingleImage(existingKos, file, type);
            }
        });

        // 4. Proses gambar tambahan (jika ada file baru yang diupload)
        if (additionalImages != null && additionalImages.length > 0) {
            for (MultipartFile imageFile : additionalImages) {
                if (imageFile != null && !imageFile.isEmpty()) {
                    // Cukup tambahkan sebagai gambar "additional"
                    saveSingleImage(existingKos, imageFile, "additional");
                }
            }
        }

        return kosRepository.save(existingKos);
    }

    private void saveSingleImage(Kos kos, MultipartFile image, String type) {
        // Validasi file individual
        validateSingleImage(image);

        String newFilename;
        try {
            String originalFilename = image.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            validateFileExtension(extension);
            newFilename = UUID.randomUUID().toString() + extension;

            Path targetLocation = imageStorageLocation.resolve(newFilename);
            Files.copy(image.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            KosImage kosImage = new KosImage();
            kosImage.setKosId(kos.getId());
            kosImage.setUrl(newFilename);
            kosImage.setType(type);
            kosImageRepository.save(kosImage);
        } catch (Exception e) {
            throw new RuntimeException("Gagal memproses gambar: " + image.getOriginalFilename(), e);
        }
    }

    private void validateSingleImage(MultipartFile image) {
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File " + image.getOriginalFilename() + " terlalu besar. Maksimal 10MB");
        }
        // ... validasi lainnya bisa ditambahkan di sini ...
    }

    private void deleteImageFile(String filename) {
        try {
            Files.deleteIfExists(imageStorageLocation.resolve(filename));
        } catch (IOException e) {
            // Log error but don't throw
            e.printStackTrace();
        }
    }

    private void validateImages(List<MultipartFile> images) {
        if (images.size() > 10) {
            throw new RuntimeException("Maximum 10 images allowed");
        }

        for (MultipartFile image : images) {
            if (image.getSize() > MAX_FILE_SIZE) {
                throw new RuntimeException("File size exceeds maximum limit of 10MB");
            }

            String filename = image.getOriginalFilename();
            if (filename == null || filename.trim().isEmpty()) {
                throw new RuntimeException("Invalid file name");
            }

            validateFileExtension(getFileExtension(filename));
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            throw new RuntimeException("Invalid file format");
        }
        return filename.substring(lastDot).toLowerCase();
    }

    private void validateFileExtension(String extension) {
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RuntimeException("Invalid file format. Allowed formats: JPG, JPEG, PNG, GIF");
        }
    }

    private void validateKosData(Kos kos) {
        if (kos.getName() == null || kos.getName().trim().isEmpty()) {
            throw new RuntimeException("Nama kos tidak boleh kosong");
        }
        if (kos.getPriceMonthly() == null || kos.getPriceMonthly().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Harga kos tidak valid");
        }
        // Add more validations as needed
    }

    public Page<Kos> getAllKos(Pageable pageable) {
        return kosRepository.findAllByOrderByIdDesc(pageable);
    }

    public Page<Kos> searchKos(String query, Pageable pageable) {
        return kosRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(query, query, pageable);
    }

    public Page<Kos> getKosByOwner(Long ownerId, Pageable pageable) {
        return kosRepository.findByOwnerId(ownerId, pageable);
    }

    public Page<Kos> getAvailableKosByType(String type, Pageable pageable) {
        return kosRepository.findByStatusAndType("available", type, pageable);
    }

    public Kos getKosById(Long id) {
        return kosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kos not found"));
    }

    public List<KosImage> getKosImages(Long kosId) {
        return kosImageRepository.findByKosId(kosId);
    }

    public List<KosImage> getKosImagesByType(Long kosId, String type) {
        return kosImageRepository.findByKosIdAndType(kosId, type);
    }

    public Page<Kos> getMyKos(Pageable pageable) {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User must be logged in to view their kos");
        }
        return kosRepository.findByOwnerId(currentUser.getId(), pageable);
    }
}