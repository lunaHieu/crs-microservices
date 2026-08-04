# Blueprint API

## auth-service (cổng 8081)
- POST /auth/login : Đăng nhập, trả về JWT (Public)
- POST /auth/register : Đăng ký tài khoản (Public)

## course-service (cổng 8082)
- GET /courses : Danh sách, có search + phân trang (Public)
- GET /courses/{id} : Chi tiết 1 môn học (Public)
- POST /courses : Thêm môn học (ADMIN)
- PUT /courses/{id} : Sửa môn học (ADMIN)
- DELETE /courses/{id} : Xoá môn học (ADMIN)
- PATCH /internal/courses/{id}/reserve-seat : Kiểm tra còn chỗ, trừ soChoConLai (API nội bộ)
- PATCH /internal/courses/{id}/release-seat : Hoàn trả 1 chỗ khi huỷ đăng ký (API nội bộ)

## registration-service (cổng 8083)
- POST /registrations : Đăng ký học phần (STUDENT)
- GET /registrations/my : Danh sách đăng ký của tôi (STUDENT)
- DELETE /registrations/{id} : Huỷ đăng ký (STUDENT/ADMIN)