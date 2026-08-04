# CRS Microservices

Hệ thống Đăng ký Học phần (Course Registration System) được xây dựng theo kiến trúc Microservices.

## Kiến trúc Hệ thống

Hệ thống bao gồm 4 microservices backend và 1 ứng dụng web frontend:

| Component | Port | Database | Trách nhiệm chính |
| :--- | :--- | :--- | :--- |
| **api-gateway** | `8080` | *(Không có DB)* | Điểm vào duy nhất, định tuyến (Routing), xác thực sơ bộ, CORS |
| **auth-service** | `8081` | `auth_db` | Quản lý Người dùng, Sinh viên, Đăng nhập, Đăng ký, phát hành & xác thực token JWT |
| **course-service** | `8082` | `course_db` | Quản lý Môn học, Tìm kiếm, Phân trang, Quản lý số chỗ còn lại |
| **registration-service** | `8083` | `registration_db` | Quản lý Đăng ký học phần, gọi API nội bộ tới `course-service` để giữ/trừ chỗ |
| **crs-frontend** | `3000` | - | Giao diện người dùng Web Application |

## Công nghệ sử dụng

- **Backend**: Java 21, Spring Boot 3+, Spring Data JPA, Spring Cloud Gateway, Spring Security JWT
- **Database**: MySQL
- **Frontend**: Web Application
- **DevOps**: Docker, Docker Compose

## Cấu trúc thư mục

```text
crs-microservices/
├── api-gateway/          # Microservice API Gateway (port 8080)
├── auth-service/         # Microservice Authentication & User Management (port 8081)
├── course-service/       # Microservice Course Management (port 8082)
├── registration-service/ # Microservice Registration Management (port 8083)
├── crs-frontend/         # Frontend Web Application
├── docs/                 # Tài liệu thiết kế hệ thống & API Specification
├── .env                  # Cấu hình biến môi trường (Local / Secret)
├── .gitignore            # Cấu hình bỏ qua file trong Git
└── README.md             # Tài liệu tổng quan dự án
```
