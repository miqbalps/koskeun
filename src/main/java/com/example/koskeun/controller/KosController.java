package com.example.koskeun.controller;

import com.example.koskeun.model.Kos;
import com.example.koskeun.model.KosImage;
import com.example.koskeun.service.KosService;
import com.example.koskeun.dto.request.BookingRequest;
import com.example.koskeun.dto.request.KosApprovalRequest;
import com.example.koskeun.dto.request.KosRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/kos")
public class KosController {

    @Autowired
    private KosService kosService;

    @GetMapping("/search")
    public String searchKos(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Kos> kosPage;

        if (query != null && !query.trim().isEmpty()) {
            kosPage = kosService.searchKos(query, pageRequest);
        } else {
            kosPage = kosService.getAllKos(pageRequest);
        }

        model.addAttribute("kosList", kosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", kosPage.getTotalPages());
        model.addAttribute("query", query);

        return "list-kos";
    }

    @GetMapping("/approval")
    public String approvalKos(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Kos> kosPage;

        if (query != null && !query.trim().isEmpty()) {
            kosPage = kosService.searchKos(query, pageRequest);
        } else {
            kosPage = kosService.getAllKos(pageRequest);
        }

        model.addAttribute("kosList", kosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", kosPage.getTotalPages());
        model.addAttribute("query", query);

        return "approval-kos";
    }

    @PostMapping("/approval/{id}")
    public String processKosApproval(
            @PathVariable("kosId") Long kosId,
            @ModelAttribute("approvalRequest") KosApprovalRequest approvalRequest,
            RedirectAttributes redirectAttributes) {
        try {
            kosService.approvalKos(kosId, approvalRequest);
            redirectAttributes.addFlashAttribute("success", "Status persetujuan Kos berhasil diperbarui.");
            // Redirect ke halaman dashboard admin atau halaman detail kos lagi
            return "redirect:/kos/approval";

        } catch (Exception e) {
            e.printStackTrace(); // Gunakan logger pada aplikasi production
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui status: " + e.getMessage());
            // Redirect kembali ke halaman sebelumnya (detail kos)
            return "redirect:/kos/detail/" + kosId;
        }
    }

    @GetMapping("/detail/{id}")
    public String kosDetail(@PathVariable("id") Long id, Model model) {
        Kos kos = kosService.getKosById(id);
        List<KosImage> images = kosService.getKosImages(id);
        List<Kos> similarKos = kosService.getAvailableKosByType(kos.getType(), PageRequest.of(0, 2)).getContent();

        model.addAttribute("kos", kos);
        model.addAttribute("images", images);
        model.addAttribute("similarKos", similarKos);
        model.addAttribute("bookingRequest", new BookingRequest());

        return "detail-kos";
    }

    @GetMapping("/my")
    public String myKos(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Kos> kosPage = kosService.getMyKos(pageRequest);

        model.addAttribute("kosList", kosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", kosPage.getTotalPages());

        return "my-kos";
    }

    @GetMapping("/add")
    public String showAddKosForm(Model model) {
        model.addAttribute("kos", new KosRequest());
        return "add-kos";
    }

    @PostMapping("/add")
    public String addKos(
            @Valid @ModelAttribute("kos") KosRequest request,
            BindingResult bindingResult,
            @RequestParam("frontBuilding") MultipartFile frontBuilding,
            @RequestParam("interior") MultipartFile interior,
            @RequestParam(value = "streetView", required = false) MultipartFile streetView,
            @RequestParam("roomFront") MultipartFile roomFront,
            @RequestParam("roomInterior") MultipartFile roomInterior,
            @RequestParam("bathroom") MultipartFile bathroom,
            @RequestParam(value = "additionalImages", required = false) MultipartFile[] additionalImages,
            @RequestParam("frontBuildingType") String frontBuildingType,
            @RequestParam("interiorType") String interiorType,
            @RequestParam(value = "streetViewType", required = false) String streetViewType,
            @RequestParam("roomFrontType") String roomFrontType,
            @RequestParam("roomInteriorType") String roomInteriorType,
            @RequestParam("bathroomType") String bathroomType,
            @RequestParam(value = "additionalImagesType", required = false) String additionalImagesType,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validasi form
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Mohon periksa kembali form anda");
            return "add-kos";
        }

        // Validasi file wajib
        if (frontBuilding.isEmpty() || interior.isEmpty() ||
                roomFront.isEmpty() || roomInterior.isEmpty() ||
                bathroom.isEmpty()) {
            model.addAttribute("error", "Foto wajib tidak boleh kosong");
            return "add-kos";
        }

        try {
            List<MultipartFile> images = new ArrayList<>();
            List<String> types = new ArrayList<>();

            // Add required images
            addImageWithType(images, types, frontBuilding, frontBuildingType);
            addImageWithType(images, types, interior, interiorType);
            addImageWithType(images, types, roomFront, roomFrontType);
            addImageWithType(images, types, roomInterior, roomInteriorType);
            addImageWithType(images, types, bathroom, bathroomType);

            // Add optional images
            if (streetView != null && !streetView.isEmpty()) {
                addImageWithType(images, types, streetView, streetViewType);
            }

            if (additionalImages != null && additionalImages.length > 0) {
                for (MultipartFile image : additionalImages) {
                    if (!image.isEmpty()) {
                        addImageWithType(images, types, image, additionalImagesType);
                    }
                }
            }

            // Convert request to entity
            Kos kos = convertToKos(request);

            // Save kos
            Kos savedKos = kosService.createKos(kos, new ArrayList<>());

            // Save images
            kosService.saveKosImages(savedKos, images, types);

            redirectAttributes.addFlashAttribute("success", "Kos berhasil ditambahkan");
            return "redirect:/kos/my";

        } catch (Exception e) {
            model.addAttribute("error", "Gagal menambahkan kos: " + e.getMessage());
            return "add-kos";
        }
    }

    private void addImageWithType(List<MultipartFile> images, List<String> types, MultipartFile file, String type) {
        if (file != null && !file.isEmpty()) {
            images.add(file);
            types.add(type);
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditKosForm(@PathVariable("id") Long id, Model model) {
        Kos kos = kosService.getKosById(id);

        // Ambil semua gambar yang terkait dengan Kos ini
        List<KosImage> allImages = kosService.getKosImages(id);

        // PERBAIKAN: Kelompokkan gambar berdasarkan tipenya menggunakan Map
        Map<String, List<KosImage>> imagesByType = allImages.stream()
                .collect(Collectors.groupingBy(KosImage::getType));

        // Kirim objek DTO untuk form binding
        model.addAttribute("kos", convertToKosRequest(kos)); // 'kos' object for the form

        // Kirim ID kos untuk action form
        model.addAttribute("id", id);

        // PERBAIKAN: Kirim Map dari gambar yang sudah dikelompokkan ke view
        model.addAttribute("existingImages", imagesByType);

        return "edit-kos";
    }

    @PostMapping("/edit/{id}")
    public String editKos(
            @PathVariable("id") Long kosId,
            @Valid @ModelAttribute("kos") KosRequest kosRequest,
            BindingResult bindingResult,
            // Terima setiap file secara terpisah dan buat menjadi tidak wajib (required =
            // false)
            @RequestParam(value = "frontBuilding", required = false) MultipartFile frontBuildingFile,
            @RequestParam(value = "interior", required = false) MultipartFile interiorFile,
            @RequestParam(value = "streetView", required = false) MultipartFile streetViewFile,
            @RequestParam(value = "roomFront", required = false) MultipartFile roomFrontFile,
            @RequestParam(value = "roomInterior", required = false) MultipartFile roomInteriorFile,
            @RequestParam(value = "bathroom", required = false) MultipartFile bathroomFile,
            @RequestParam(value = "additionalImages", required = false) MultipartFile additionalImageFiles,
            RedirectAttributes redirectAttributes,
            Model model // Tambahkan Model untuk mengembalikan data jika validasi gagal
    ) {
        if (bindingResult.hasErrors()) {
            // Jika validasi dasar gagal, kembalikan ke form dengan pesan error
            // Kita juga perlu mengisi kembali model dengan gambar yang sudah ada
            prepareEditFormModel(kosId, model);
            model.addAttribute("error", "Data yang Anda masukkan tidak valid. Mohon periksa kembali.");
            return "edit-kos";
        }

        try {
            // Kelompokkan file-file yang di-upload ke dalam sebuah Map
            Map<String, MultipartFile> singleImageMap = Map.of(
                    "front_house", frontBuildingFile,
                    "inside_house", interiorFile,
                    "road", streetViewFile,
                    "front_room", roomFrontFile,
                    "inside_room", roomInteriorFile,
                    "bathroom", bathroomFile,
                    "additional", additionalImageFiles);

            // Panggil service update yang sudah diperbarui
            kosService.updateKosAndImages(kosId, kosRequest, singleImageMap);

            redirectAttributes.addFlashAttribute("success", "Kos berhasil diperbarui.");
            return "redirect:/kos/detail/" + kosId;

        } catch (Exception e) {
            e.printStackTrace(); // Log error
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui kos: " + e.getMessage());
            return "redirect:/kos/edit/" + kosId;
        }
    }

    // Helper method untuk menghindari duplikasi kode saat menyiapkan model untuk
    // form edit
    private void prepareEditFormModel(Long kosId, Model model) {
        Kos kos = kosService.getKosById(kosId);
        List<KosImage> allImages = kosService.getKosImages(kosId);
        Map<String, List<KosImage>> imagesByType = allImages.stream().collect(Collectors.groupingBy(KosImage::getType));

        model.addAttribute("kos", convertToKosRequest(kos));
        model.addAttribute("kosId", kosId);
        model.addAttribute("existingImages", imagesByType);
    }

    @PostMapping("/delete/{id}")
    public String deleteKos(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check if kos has any linked transactions
            if (kosService.hasActiveTransactions(id)) {
                redirectAttributes.addFlashAttribute("error",
                        "Tidak dapat menghapus kos karena memiliki transaksi aktif");
                return "redirect:/kos/detail/" + id;
            }

            kosService.deleteKos(id);
            redirectAttributes.addFlashAttribute("success", "Kos berhasil dihapus");
            return "redirect:/kos/my";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/kos/detail/" + id;
        }
    }

    @PostMapping("/{kosId}/images/{imageId}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteKosImage(@PathVariable("kosId") Long kosId, @PathVariable("imageId") Long imageId) {
        try {
            kosService.deleteKosImage(kosId, imageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public String getKosByType(
            @PathVariable("type") String type,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Kos> kosPage = kosService.getAvailableKosByType(type, pageRequest);

        model.addAttribute("kosList", kosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", kosPage.getTotalPages());
        model.addAttribute("type", type);

        return "list-kos";
    }

    private Kos convertToKos(KosRequest request) {
        Kos kos = new Kos();
        kos.setName(request.getName());
        kos.setPriceMonthly(request.getPriceMonthly());
        kos.setDescription(request.getDescription());
        kos.setType(request.getType());
        kos.setCategory(request.getCategory());
        kos.setAddress(request.getAddress());
        kos.setLatitude(request.getLatitude());
        kos.setLongitude(request.getLongitude());
        kos.setStatus(request.getStatus());
        return kos;
    }

    private KosRequest convertToKosRequest(Kos kos) {
        KosRequest request = new KosRequest();
        request.setName(kos.getName());
        request.setPriceMonthly(kos.getPriceMonthly());
        request.setDescription(kos.getDescription());
        request.setType(kos.getType());
        request.setCategory(kos.getCategory());
        request.setAddress(kos.getAddress());
        request.setLatitude(kos.getLatitude());
        request.setLongitude(kos.getLongitude());
        request.setStatus(kos.getStatus());
        return request;
    }
}
