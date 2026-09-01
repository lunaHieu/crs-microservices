package vn.edu.crs.registrationservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.registrationservice.client.CourseClient;
import vn.edu.crs.registrationservice.dto.RegistrationRequestDTO;
import vn.edu.crs.registrationservice.entity.Registration;
import vn.edu.crs.registrationservice.repository.RegistrationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final String DA_DANG_KY = "DA_DANG_KY";
    private static final String DA_HUY = "DA_HUY";

    private final RegistrationRepository registrationRepository;
    private final CourseClient courseClient;

    @Transactional
    public Registration register(RegistrationRequestDTO request) {

        boolean existed =
                registrationRepository.existsByStudentIdAndCourseIdAndTrangThai(
                        request.getStudentId(),
                        request.getCourseId(),
                        DA_DANG_KY
                );

        if (existed) {
            throw new IllegalStateException(
                    "Sinh vien da dang ky mon hoc nay"
            );
        }

        courseClient.reserveSeat(request.getCourseId());

        Registration registration = new Registration();
        registration.setStudentId(request.getStudentId());
        registration.setCourseId(request.getCourseId());
        registration.setTrangThai(DA_DANG_KY);
        registration.setNgayDangKy(LocalDateTime.now());

        return registrationRepository.save(registration);
    }

    public List<Registration> getByStudentId(Long studentId) {
        return registrationRepository.findByStudentId(studentId);
    }

    public List<Registration> getMyRegistrations(Long studentId) {
        return registrationRepository.findByStudentId(studentId);
    }

    @Transactional
    public Registration cancel(Long registrationId) {

        Registration registration = registrationRepository
                .findById(registrationId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Khong tim thay dang ky id = " + registrationId
                        )
                );

        if (DA_HUY.equals(registration.getTrangThai())) {
            throw new IllegalStateException(
                    "Dang ky nay da duoc huy truoc do"
            );
        }

        courseClient.releaseSeat(registration.getCourseId());

        registration.setTrangThai(DA_HUY);

        return registrationRepository.save(registration);
    }
}
