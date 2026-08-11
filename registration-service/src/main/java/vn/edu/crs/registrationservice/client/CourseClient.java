package vn.edu.crs.registrationservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class CourseClient {

    private final RestTemplate restTemplate;

    @Value("${course-service.base-url}")
    private String courseServiceBaseUrl;

    public void reserveSeat(Long courseId) {
        String url = courseServiceBaseUrl
                + "/internal/courses/"
                + courseId
                + "/reserve-seat";

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    null,
                    Void.class
            );
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IllegalStateException("Mon hoc khong ton tai");
        } catch (HttpClientErrorException.Conflict ex) {
            throw new IllegalStateException("Mon hoc da het cho, khong the dang ky");
        } catch (ResourceAccessException ex) {
            throw new IllegalStateException("Khong the ket noi den course-service");
        }
    }

    public void releaseSeat(Long courseId) {
        String url = courseServiceBaseUrl
                + "/internal/courses/"
                + courseId
                + "/release-seat";

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    null,
                    Void.class
            );
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IllegalStateException("Mon hoc khong ton tai");
        } catch (ResourceAccessException ex) {
            throw new IllegalStateException("Khong the ket noi den course-service");
        }
    }
}