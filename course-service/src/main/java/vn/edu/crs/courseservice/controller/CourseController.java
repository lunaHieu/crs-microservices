package vn.edu.crs.courseservice.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courses")

public class CourseController {

    @GetMapping
    public List<Map<String, Object>> getMockCourse() {
        return List.of(
                Map.of(
                        "id", 1,
                        "tenMonHoc", "Lap trinh java co ban",
                        "soTinChi", 3,
                        "soChoToiDa", 40,
                        "soChoConLai", 12
                ),
                Map.of(
                        "id", 2,
                        "tenMonHoc", "Co so du lieu",
                        "soTinChi", 4,
                        "soChoToiDa", 35,
                        "soChoConLai", 0
                )
        );
    }
}
