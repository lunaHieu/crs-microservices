# Blueprint API Specification (CRS Microservices)

Tài liệu này định nghĩa chi tiết hợp đồng API (API Contracts) của hệ thống CRS Microservices.

> [!IMPORTANT]
> Từ Buổi 4, tất cả ứng dụng phía Client (Web Frontend, Mobile App, Postman) **bắt buộc phải gửi request qua API Gateway tại cổng 8080** (`http://localhost:8080`).
> Các cổng service nội bộ (`8081`, `8082`, `8083`) chỉ sử dụng cho giao tiếp server-to-server hoặc mục đích phát triển.

---

## Bảng tổng hợp Hợp đồng API (API Summary Table)

| Group | Method | External URL (via Gateway :8080) | Internal Target URL | Authentication | Role Required | Description |
| :--- | :---: | :--- | :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/auth/login` | `http://localhost:8081/auth/login` | Public | Anyone | Đăng nhập tài khoản, nhận Token JWT |
| **Course** | `GET` | `/api/courses` | `http://localhost:8082/courses` | Public | Anyone | Lấy danh sách môn học (có search & phân trang) |
| **Course** | `GET` | `/api/courses/{id}` | `http://localhost:8082/courses/{id}` | Public | Anyone | Xem thông tin chi tiết 1 môn học |
| **Course** | `POST` | `/api/courses` | `http://localhost:8082/courses` | Bearer JWT | `ADMIN` | Tạo môn học mới |
| **Course** | `PUT` | `/api/courses/{id}` | `http://localhost:8082/courses/{id}` | Bearer JWT | `ADMIN` | Cập nhật thông tin môn học |
| **Course** | `DELETE`| `/api/courses/{id}` | `http://localhost:8082/courses/{id}` | Bearer JWT | `ADMIN` | Xóa môn học |
| **Registration** | `POST` | `/api/registrations` | `http://localhost:8083/registrations` | Bearer JWT | Authenticated | Đăng ký môn học (tự động giữ chỗ) |
| **Registration** | `GET` | `/api/registrations/student/{studentId}` | `http://localhost:8083/registrations/student/{studentId}` | Bearer JWT | Authenticated | Xem danh sách môn học đã đăng ký của sinh viên |
| **Registration** | `DELETE`| `/api/registrations/{id}` | `http://localhost:8083/registrations/{id}` | Bearer JWT | Authenticated | Hủy đăng ký học phần (tự động hoàn trả chỗ) |
| **Partner** | `GET` | `/api/public/courses` | `http://localhost:8082/courses` | Header `X-API-KEY` | Partner | Lấy danh sách môn học dành cho đối tác tích hợp |
| **Internal** | `PATCH` | *(Nội bộ - Không expose Gateway)* | `http://localhost:8082/internal/courses/{id}/reserve-seat` | None | Internal Service | Giảm 1 chỗ trống khi đăng ký thành công |
| **Internal** | `PATCH` | *(Nội bộ - Không expose Gateway)* | `http://localhost:8082/internal/courses/{id}/release-seat` | None | Internal Service | Hoàn 1 chỗ trống khi hủy đăng ký |

---

## Bảng Mã trạng thái HTTP (HTTP Status Codes)

- **`200 OK`**: Request thành công (lấy dữ liệu, cập nhật thành công, hủy đăng ký thành công).
- **`201 Created`**: Tạo thành công tài nguyên mới (Tạo môn học, Đăng ký học phần thành công).
- **`204 No Content`**: Xóa tài nguyên thành công (Xóa môn học).
- **`400 Bad Request`**: Dữ liệu gửi lên không hợp lệ (Validation error, trùng tên môn học khi tạo).
- **`401 Unauthorized`**: Chưa xác thực (Gateway phát hiện thiếu Header `Authorization` hoặc sai username/password khi login).
- **`403 Forbidden`**: Không có quyền truy cập (Dùng token `STUDENT` gọi API `ADMIN` hoặc thiếu/sai Header `X-API-KEY`).
- **`404 Not Found`**: Không tìm thấy tài nguyên (Môn học hoặc Bản ghi đăng ký không tồn tại).
- **`409 Conflict`**: Xung đột nghiệp vụ (Đăng ký trùng môn, môn học đã hết chỗ, bản ghi đăng ký đã hủy trước đó, hoặc service nội bộ không thể kết nối).

---

## Chi tiết Endpoints & Payload Contracts

### A. Authentication APIs (`auth-service`)

#### 1. Đăng nhập hệ thống
- **URL**: `POST http://localhost:8080/api/auth/login`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **Response `200 OK`**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIs...",
    "username": "admin",
    "role": "ADMIN"
  }
  ```
- **Response `401 Unauthorized`**:
  ```json
  {
    "timestamp": "2026-08-13T10:00:00Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Sai username hoac password",
    "path": "/auth/login"
  }
  ```

---

### B. Course APIs (`course-service`)

#### 1. Danh sách môn học (Phân trang & Tìm kiếm)
- **URL**: `GET http://localhost:8080/api/courses`
- **Query Parameters**:
  - `keyword` *(string, optional)*: Tìm kiếm theo tên môn học (không phân biệt hoa thường).
  - `page` *(int, optional, default: 0)*: Số trang.
  - `size` *(int, optional, default: 20)*: Số phần tử trên 1 trang.
  - `sort` *(string, optional)*: Trường sắp xếp (vd: `tenMonHoc,asc`).
- **Response `200 OK`**:
  ```json
  {
    "content": [
      {
        "id": 1,
        "tenMonHoc": "Lập trình Java",
        "soTinChi": 3,
        "soChoToiDa": 30,
        "soChoConLai": 29
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": 1,
    "totalPages": 1
  }
  ```

#### 2. Chi tiết môn học
- **URL**: `GET http://localhost:8080/api/courses/{id}`
- **Response `200 OK`**:
  ```json
  {
    "id": 1,
    "tenMonHoc": "Lập trình Java",
    "soTinChi": 3,
    "soChoToiDa": 30,
    "soChoConLai": 29
  }
  ```
- **Response `404 Not Found`**:
  ```json
  {
    "message": "Khong tim thay mon hoc id = 999"
  }
  ```

#### 3. Tạo mới môn học
- **URL**: `POST http://localhost:8080/api/courses`
- **Headers**: `Authorization: Bearer <ADMIN_JWT_TOKEN>`
- **Request Body**:
  ```json
  {
    "tenMonHoc": "Kiến trúc Microservices",
    "soTinChi": 3,
    "soChoToiDa": 40
  }
  ```
- **Response `201 Created`**:
  ```json
  {
    "id": 12,
    "tenMonHoc": "Kiến trúc Microservices",
    "soTinChi": 3,
    "soChoToiDa": 40,
    "soChoConLai": 40
  }
  ```
- **Response `400 Bad Request`**: `{"message": "Ten mon hoc da ton tai"}`
- **Response `403 Forbidden`**: Trả về từ `course-service` khi token gửi lên mang Role `STUDENT`.

#### 4. Cập nhật môn học
- **URL**: `PUT http://localhost:8080/api/courses/{id}`
- **Headers**: `Authorization: Bearer <ADMIN_JWT_TOKEN>`
- **Request Body**:
  ```json
  {
    "tenMonHoc": "Kiến trúc Microservices Nâng cao",
    "soTinChi": 4,
    "soChoToiDa": 50
  }
  ```
- **Response `200 OK`**: Trả về `CourseDTO` đã cập nhật.

#### 5. Xóa môn học
- **URL**: `DELETE http://localhost:8080/api/courses/{id}`
- **Headers**: `Authorization: Bearer <ADMIN_JWT_TOKEN>`
- **Response `204 No Content`**

---

### C. Registration APIs (`registration-service`)

#### 1. Đăng ký học phần
- **URL**: `POST http://localhost:8080/api/registrations`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Request Body**:
  ```json
  {
    "studentId": 1,
    "courseId": 11
  }
  ```
- **Response `201 Created`**:
  ```json
  {
    "id": 5,
    "studentId": 1,
    "courseId": 11,
    "trangThai": "DA_DANG_KY",
    "ngayDangKy": "2026-08-13T10:15:30.123"
  }
  ```
- **Response `409 Conflict`**:
  - Khi sinh viên đã đăng ký môn này trước đó: `{"message": "Sinh vien da dang ky mon hoc nay"}`
  - Khi môn học đã hết chỗ: `{"message": "Mon hoc da het cho, khong the dang ky"}`
  - Khi không tìm thấy môn học: `{"message": "Mon hoc khong ton tai"}`
  - Khi không thể kết nối tới `course-service`: `{"message": "Khong the ket noi den course-service"}`

#### 2. Xem danh sách môn học đã đăng ký của sinh viên
- **URL**: `GET http://localhost:8080/api/registrations/student/{studentId}`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Response `200 OK`**:
  ```json
  [
    {
      "id": 5,
      "studentId": 1,
      "courseId": 11,
      "trangThai": "DA_DANG_KY",
      "ngayDangKy": "2026-08-13T10:15:30.123"
    }
  ]
  ```

#### 3. Hủy đăng ký học phần
- **URL**: `DELETE http://localhost:8080/api/registrations/{id}`
- **Headers**: `Authorization: Bearer <JWT_TOKEN>`
- **Response `200 OK`**:
  ```json
  {
    "id": 5,
    "studentId": 1,
    "courseId": 11,
    "trangThai": "DA_HUY",
    "ngayDangKy": "2026-08-13T10:15:30.123"
  }
  ```
- **Response `404 Not Found`**: `{"message": "Khong tim thay dang ky id = 999"}`
- **Response `409 Conflict`**: `{"message": "Dang ky nay da duoc huy truoc do"}`

---

### D. Partner / Public APIs

#### 1. Lấy danh sách môn học cho Đối tác
- **URL**: `GET http://localhost:8080/api/public/courses`
- **Headers**: `X-API-KEY: crs-partner-key-2026`
- **Response `200 OK`**: Danh sách/Trang môn học tương tự `GET /api/courses`.
- **Response `403 Forbidden`**: Thiếu hoặc sai header `X-API-KEY` (Chặn ngay tại Gateway).

---

### E. Internal APIs (Server-to-Server Only)

Các API này phục vụ truyền thông trực tiếp giữa `registration-service` và `course-service`, **không được cấu hình route qua API Gateway**.

#### 1. Trừ chỗ môn học (Reserve Seat)
- **URL**: `PATCH http://localhost:8082/internal/courses/{id}/reserve-seat`
- **Response `200 OK`**: Trả về `CourseDTO` đã trừ `soChoConLai`.
- **Response `404 Not Found`**: Không tìm thấy môn học.
- **Response `409 Conflict`**: Môn học đã hết chỗ (`soChoConLai <= 0`).

#### 2. Hoàn trả chỗ môn học (Release Seat)
- **URL**: `PATCH http://localhost:8082/internal/courses/{id}/release-seat`
- **Response `200 OK`**: Trả về `CourseDTO` đã tăng `soChoConLai`.
- **Response `404 Not Found`**: Không tìm thấy môn học.