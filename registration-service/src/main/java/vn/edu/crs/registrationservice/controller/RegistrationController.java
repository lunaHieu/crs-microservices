package vn.edu.crs.registrationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.registrationservice.dto.RegistrationRequestDTO;
import vn.edu.crs.registrationservice.entity.Registration;
import vn.edu.crs.registrationservice.service.RegistrationService;

import java.util.List;

@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Registration register(
            @Valid @RequestBody RegistrationRequestDTO request
    ) {
        return registrationService.register(request);
    }

    @GetMapping("/student/{studentId}")
    public List<Registration> getByStudentId(
            @PathVariable Long studentId
    ) {
        return registrationService.getByStudentId(studentId);
    }

    @DeleteMapping("/{id}")
    public Registration cancel(
            @PathVariable Long id
    ) {
        return registrationService.cancel(id);
    }
}