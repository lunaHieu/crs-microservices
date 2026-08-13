# Thiết kế Biên giới Service & Kiến trúc Ranh giới (Service Boundary Design)

Tài liệu này quy định kiến trúc ranh giới (Service Boundary), quyền sở hữu dữ liệu (Data Ownership), cơ chế giao tiếp giữa các service (Inter-Service Communication) và mô hình bảo mật của hệ thống CRS Microservices sau Buổi 4.

---

## 1. Danh sách Service & Quyền sở hữu Dữ liệu (Data Ownership)

Hệ thống được thiết kế theo nguyên tắc **Database per Service**. Mỗi Microservice quản lý một cơ sở dữ liệu hoàn toàn độc lập, đảm bảo tính cô lập dữ liệu (data isolation) và độc lập trong triển khai.

| Service | Port | Database | Data Entities Owned | Trách nhiệm chính |
| :--- | :---: | :---: | :--- | :--- |
| **`api-gateway`** | `8080` | *(Không DB)* | *(Không sở hữu dữ liệu)* | Single Entry Point cho Client, định tuyến (RewritePath), CORS, kiểm tra Authorization header sơ bộ (Layer 1), kiểm tra Partner API Key. |
| **`auth-service`** | `8081` | `auth_db` | - `User` (`app_user`) <br>- `Student` (`student`) | Quản lý người dùng, sinh viên, tài khoản, mã hóa mật khẩu BCrypt, seed dữ liệu mẫu, đăng nhập (`/auth/login`) và phát hành JWT Token. |
| **`course-service`** | `8082` | `course_db` | - `Course` (`course`) | Quản lý thông tin môn học, tìm kiếm & phân trang, quản lý số chỗ (`soChoToiDa`, `soChoConLai`), tự verify JWT & phân quyền RBAC (ADMIN), cung cấp API nội bộ điều phối chỗ. |
| **`registration-service`**| `8083` | `registration_db` | - `Registration` (`registration`) | Quản lý lịch sử và trạng thái đăng ký môn học (`DA_DANG_KY`, `DA_HUY`), tự verify JWT, thực hiện cuộc gọi REST nội bộ sang `course-service` để giữ/trả chỗ. |

---

## 2. Các Quy tắc Ranh giới Cốt lõi (Core Boundary Rules)

1. **Không Truy vấn Chéo Cơ sở Dữ liệu (No Cross-Database Query)**:
   - `registration-service` tuyệt đối không được đọc/ghi trực tiếp vào `course_db` hay `auth_db`.
   - `course-service` tuyệt đối không được đọc/ghi vào `auth_db` hay `registration_db`.

2. **Không Thiết lập Quan hệ JPA xuyên Service (No Cross-Service JPA Relationships)**:
   - Các Entity như `Registration` không dùng các annotation `@ManyToOne` hay `@OneToOne` tới `Course` hay `Student`.
   - Tất cả các liên kết giữa các miền dữ liệu chỉ được lưu dưới dạng **ID đơn thuần** (ví dụ: `studentId`, `courseId`).

3. **Giao tiếp qua Hợp đồng REST API (Communication via REST APIs)**:
   - Mọi sự trao đổi thông tin hoặc thay đổi trạng thái giữa các service bắt buộc phải đi qua HTTP REST API chính thức.
   - Ví dụ: Khi sinh viên đăng ký, `registration-service` bắt buộc phải gọi sang `course-service` bằng HTTP `PATCH` để giữ chỗ.

4. **Ranh giới API Nội bộ (Internal API Boundary)**:
   - Các API có tiền tố `/internal/**` (ví dụ: `/internal/courses/{id}/reserve-seat`) được định nghĩa dành riêng cho giao tiếp server-to-server.
   - API Gateway **không bao giờ cấu hình route** cho đường dẫn `/internal/**`. Do đó, Client bên ngoài không thể trực tiếp gọi tới các API nội bộ này.

5. **Mô hình Bảo mật Nhân bản Tự chủ (Deliberate Security Duplication)**:
   - `JwtAuthFilter` được triển khai tại cả `course-service` và `registration-service`.
   - Đây là sự trùng lặp cố ý (deliberate duplication) nhằm đảm bảo nguyên tắc Zero Trust: Mỗi business service tự bảo vệ tài nguyên của mình bằng cách tự giải mã và kiểm tra tính hợp lệ của JWT, không phụ thuộc hoàn toàn vào API Gateway.

---

## 3. Ranh giới Bảo mật 2 Tầng (2-Layer Security Boundary)

```mermaid
graph TD
    Client[Client / External Call] -->|1. HTTP Request| Gateway[API Gateway - Port 8080]
    
    subgraph Layer 1: API Gateway
        Gateway --> AuthFilter{AuthHeaderFilter}
        AuthFilter -->|Missing Auth Header| R401[Return 401 Unauthorized]
        AuthFilter --> ApiKeyCheck{ApiKeyFilter - Partner Route?}
        ApiKeyCheck -->|Missing/Wrong Key| R403[Return 403 Forbidden]
    end
    
    ApiKeyCheck -->|Valid Pre-check| Service[Target Microservice :8082 / :8083]
    
    subgraph Layer 2: Business Service Security
        Service --> JwtFilter[JwtAuthFilter]
        JwtFilter -->|Invalid Token| S401[Return 401 Unauthorized]
        JwtFilter --> SecurityRules{Spring Security & RBAC}
        SecurityRules -->|Forbidden Role| S403[Return 403 Forbidden]
        SecurityRules -->|Authorized| Controller[Business Logic / Controller]
    end
```

- **Tầng 1 (Gateway Boundary)**:
  - `AuthHeaderFilter`: Kiểm tra sự tồn tại của header `Authorization` đối với các protected routes (`POST/PUT/DELETE /api/courses`, `/api/registrations/**`). Nếu không có, phản hồi ngay `401 Unauthorized`.
  - `ApiKeyFilter`: Kiểm tra header `X-API-KEY` tại route `/api/public/courses`. Nếu sai hoặc thiếu key, phản hồi ngay `403 Forbidden`.
- **Tầng 2 (Service Boundary)**:
  - `JwtAuthFilter`: Tự xác minh chữ ký HMAC-SHA256 của JWT bằng `JWT_SECRET`, giải mã `username` và `role`.
  - `SecurityConfig` tại `course-service`: Yêu cầu Role `ADMIN` cho các hành động chỉnh sửa môn học (`POST/PUT/DELETE`).

---

## 4. Các Luồng Nghiệp vụ Hệ thống (System Flow Diagrams)

### Flow 1: Luồng Đăng ký Học phần (Course Registration Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Gateway as API Gateway (8080)
    participant Reg as Registration Service (8083)
    participant RegDB as registration_db
    participant Course as Course Service (8082)
    participant CourseDB as course_db

    Client->>Gateway: POST /api/registrations (Bearer JWT)
    Gateway->>Gateway: AuthHeaderFilter (Kiểm tra có Header Authorization)
    Gateway->>Reg: Forward request -> POST /registrations
    Reg->>Reg: JwtAuthFilter (Verify JWT & trích xuất User)
    Reg->>RegDB: Kiểm tra trùng lặp (studentId, courseId, trangThai = 'DA_DANG_KY')
    alt Đã đăng ký trước đó
        Reg-->>Client: 409 Conflict ("Sinh vien da dang ky mon hoc nay")
    else Chưa đăng ký
        Reg->>Course: HTTP PATCH /internal/courses/{id}/reserve-seat
        Course->>CourseDB: Kiểm tra soChoConLai > 0
        alt Hết chỗ hoặc môn học không tồn tại
            CourseDB-->>Course: Hết chỗ / Not Found
            Course-->>Reg: 409 Conflict / 404 Not Found
            Reg-->>Client: 409 Conflict ("Mon hoc da het cho...")
        else Còn chỗ
            CourseDB->>CourseDB: Giảm soChoConLai - 1
            Course-->>Reg: 200 OK (CourseDTO updated)
            Reg->>RegDB: Lưu bản ghi Registration (trangThai = 'DA_DANG_KY', ngayDangKy = NOW)
            Reg-->>Gateway: 201 Created (Registration)
            Gateway-->>Client: 201 Created
        end
    end
```

---

### Flow 2: Luồng Hủy Đăng ký Học phần (Course Cancellation Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Gateway as API Gateway (8080)
    participant Reg as Registration Service (8083)
    participant RegDB as registration_db
    participant Course as Course Service (8082)
    participant CourseDB as course_db

    Client->>Gateway: DELETE /api/registrations/{id} (Bearer JWT)
    Gateway->>Gateway: AuthHeaderFilter (Check Authorization Header)
    Gateway->>Reg: Forward request -> DELETE /registrations/{id}
    Reg->>Reg: JwtAuthFilter (Verify JWT)
    Reg->>RegDB: Tìm Registration theo id
    alt Không tìm thấy
        Reg-->>Client: 404 Not Found ("Khong tim thay dang ky...")
    else Tìm thấy
        Reg->>Reg: Kiểm tra trangThai == 'DA_HUY'
        alt Đã hủy từ trước
            Reg-->>Client: 409 Conflict ("Dang ky nay da duoc huy truoc do")
        else Trạng thái DA_DANG_KY
            Reg->>Course: HTTP PATCH /internal/courses/{id}/release-seat
            Course->>CourseDB: Tăng soChoConLai + 1 (nếu < soChoToiDa)
            Course-->>Reg: 200 OK (CourseDTO updated)
            Reg->>RegDB: Cập nhật trangThai = 'DA_HUY'
            Reg-->>Gateway: 200 OK (Registration updated)
            Gateway-->>Client: 200 OK
        end
    end
```

---

### Flow 3: Luồng Đăng nhập & Xác thực JWT (JWT Authentication Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Gateway as API Gateway (8080)
    participant Auth as Auth Service (8081)
    participant AuthDB as auth_db

    Client->>Gateway: POST /api/auth/login (username, password)
    Gateway->>Auth: Forward request -> POST /auth/login
    Auth->>AuthDB: Tim User theo username
    Auth->>Auth: BCrypt Password Encoder Matches?
    alt Sai username hoặc password
        Auth-->>Client: 401 Unauthorized ("Sai username hoac password")
    else Hợp lệ
        Auth->>Auth: Tạo JWT Token (subject=username, claim role=ROLE)
        Auth-->>Gateway: 200 OK (token, username, role)
        Gateway-->>Client: 200 OK (token, username, role)
    end
```

---

### Flow 4: Luồng API Đối tác (Partner API Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Partner
    participant Gateway as API Gateway (8080)
    participant Course as Course Service (8082)

    Partner->>Gateway: GET /api/public/courses (Header X-API-KEY)
    Gateway->>Gateway: ApiKeyFilter (Kiểm tra X-API-KEY == PARTNER_KEY)
    alt Thiếu hoặc Sai Key
        Gateway-->>Partner: 403 Forbidden
    else API Key Hợp lệ
        Gateway->>Gateway: RewritePath -> /courses
        Gateway->>Course: GET /courses
        Course-->>Gateway: 200 OK (Page CourseDTO)
        Gateway-->>Partner: 200 OK
    end
```