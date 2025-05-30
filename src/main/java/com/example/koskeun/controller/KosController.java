package com.example.koskeun.controller;

import com.example.koskeun.model.Kos;
import com.example.koskeun.model.KosImage;
import com.example.koskeun.service.KosService;
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

import java.util.List;

@Controller
@RequestMapping("/kos")
public class KosController {

    @Autowired
    private KosService kosService;

    @GetMapping("/search")
    public String searchKos(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
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

    @GetMapping("/detail/{id}")
    public String kosDetail(@PathVariable Long id, Model model) {
        Kos kos = kosService.getKosById(id);
        List<KosImage> images = kosService.getKosImages(id);
        List<Kos> similarKos = kosService.getAvailableKosByType(kos.getType(), PageRequest.of(0, 2)).getContent();

        model.addAttribute("kos", kos);
        model.addAttribute("images", images);
        model.addAttribute("similarKos", similarKos);

        return "detail-kos";
    }

    @GetMapping("/my")
    public String myKos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
            @RequestParam("images") List<MultipartFile> images,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "add-kos";
        }

        try {
            Kos kos = convertToKos(request);
            Kos savedKos = kosService.createKos(kos, images);
            redirectAttributes.addFlashAttribute("success", "Kos berhasil ditambahkan");
            return "redirect:/kos/detail/" + savedKos.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/kos/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditKosForm(@PathVariable Long id, Model model) {
        Kos kos = kosService.getKosById(id);
        List<KosImage> images = kosService.getKosImages(id);

        model.addAttribute("kos", convertToKosRequest(kos));
        model.addAttribute("images", images);

        return "edit-kos";
    }

    @PostMapping("/edit/{id}")
    public String editKos(
            @PathVariable Long id,
            @Valid @ModelAttribute("kos") KosRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "edit-kos";
        }

        try {
            Kos kos = convertToKos(request);
            kosService.updateKos(id, kos, images);
            redirectAttributes.addFlashAttribute("success", "Kos berhasil diperbarui");
            return "redirect:/kos/detail/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/kos/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteKos(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
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
    public ResponseEntity<?> deleteKosImage(@PathVariable Long kosId, @PathVariable Long imageId) {
        try {
            kosService.deleteKosImage(kosId, imageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public String getKosByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
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
        kos.setRoomCount(request.getRoomCount());
        kos.setRoomAvailable(request.getRoomAvailable());
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
        request.setRoomCount(kos.getRoomCount());
        request.setRoomAvailable(kos.getRoomAvailable());
        request.setAddress(kos.getAddress());
        request.setLatitude(kos.getLatitude());
        request.setLongitude(kos.getLongitude());
        request.setStatus(kos.getStatus());
        return request;
    }
}
