<|current_file_content|>
package code.uz.controller;


import code.uz.dto.ProfileRequestDTO;
import code.uz.dto.ProfileResponseDTO;
import code.uz.dto.ResponseDTO;
import code.uz.group_interface.OnUpdate;
import code.uz.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ProfileService profileService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<List<ProfileResponseDTO>>> getAllProfiles() {
        return ResponseEntity.ok(profileService.getAllProfiles());
    }

    @GetMapping("/byId/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<ProfileResponseDTO>> getProfileById(@PathVariable("id") String id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<ProfileResponseDTO>> updateProfile(@PathVariable("id") String id, @Validated(OnUpdate.class) @RequestBody ProfileRequestDTO profileRequestDTO) {
        return ResponseEntity.ok(profileService.updateDetailsForAdmin(id, profileRequestDTO));
    }

<|/current_file_content|>