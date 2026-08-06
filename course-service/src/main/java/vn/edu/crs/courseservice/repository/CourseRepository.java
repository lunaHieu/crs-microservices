package vn.edu.crs.courseservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.crs.courseservice.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByTenMonHocIgnoreCase(String tenMonHoc);
}