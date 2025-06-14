package com.example.koskeun.controller;

import com.example.koskeun.dto.request.UserRequest; // Ganti nama DTO jika berbeda
import com.example.koskeun.model.User;
import com.example.koskeun.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping; // Import RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile") // <--- Tambahkan ini
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * URL asli: GET /profile
     * URL baru: GET /profile (hasil dari @RequestMapping + @GetMapping)
     */
    @GetMapping("") // <--- Ubah dari "/profile" menjadi ""
    public String profile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * URL asli: GET /profile/edit
     * URL baru: GET /profile/edit (hasil dari @RequestMapping + @GetMapping)
     */
    @GetMapping("/edit") // <--- Ubah dari "/profile/edit" menjadi "/edit"
    public String showEditProfileForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());

        // Buat DTO dari entity User
        UserRequest userRequest = new UserRequest();
        userRequest.setFullName(user.getFullName());
        userRequest.setPhoneNumber(user.getPhoneNumber());
        userRequest.setGender(user.getGender());
        userRequest.setBirthDate(user.getBirthDate());
        userRequest.setOccupation(user.getOccupation());
        userRequest.setInstitutionName(user.getInstitutionName());
        userRequest.setCityOrigin(user.getCityOrigin());
        userRequest.setMaritalStatus(user.getMaritalStatus());
        userRequest.setLastEducation(user.getLastEducation());
        userRequest.setEmergencyContact(user.getEmergencyContact());

        model.addAttribute("user", userRequest);
        return "edit-profile";
    }

    /**
     * URL asli: POST /profile/edit
     * URL baru: POST /profile/edit (hasil dari @RequestMapping + @PostMapping)
     */
    @PostMapping("/edit") // <--- Ubah dari "/profile/edit" menjadi "/edit"
    public String updateProfile(@ModelAttribute("user") UserRequest userRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserProfile(userDetails.getUsername(), userRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Profil berhasil diperbarui!");
            // Redirect tetap ke /profile karena itu adalah URL absolut
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui profil: " + e.getMessage());
            // Redirect tetap ke /profile/edit
            return "redirect:/profile/edit";
        }
    }
}