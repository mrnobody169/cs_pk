# SmartNote Backend — Hướng dẫn chạy và test

## 1. Yêu cầu môi trường

| Phần mềm | Phiên bản tối thiểu |
|----------|-------------------|
| Java     | 1.8               |
| Maven    | 3.6+              |
| PostgreSQL | 13+             |
| (Optional) IntelliJ IDEA / VS Code |  |

---

## 2. Thiết lập Database

### Bước 2.1 — Tạo database
Mở **pgAdmin** hoặc **psql** và chạy:
```sql
CREATE DATABASE pokertrack;
```

### Bước 2.2 — Tạo schema (chạy thủ công)
Chạy file SQL đã có trong project:
```bash
psql -U postgres -d pokertrack -f database/schema.sql
```
Hoặc mở file `database/schema.sql` trong pgAdmin và chạy (Run > Execute Script).

### Bước 2.3 — Seed dữ liệu test (thủ công)
Chạy các lệnh SQL sau để tạo user test:
```sql
-- Password: "123456" (đã bcrypt)
INSERT INTO users (email, password_hash, display_name)
VALUES ('admin@test.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOopKQcR5D0QSq8J3d.6pmJL/Bx2nEOZS', 'Admin User');

-- Tạo group test
INSERT INTO groups (name, created_by) VALUES ('Nhóm Poker Test', 1);

-- Gán user làm ADMIN của group
INSERT INTO group_members (group_id, user_id, role) VALUES (1, 1, 'ADMIN');

-- Tạo một vài player
INSERT INTO players (group_id, name, alias) VALUES
  (1, 'Nguyễn Văn A', 'A'),
  (1, 'Trần Thị B', 'B'),
  (1, 'Lê Văn C', 'C');
```

---

## 3. Cấu hình ứng dụng

Mở file `backend/src/main/resources/application.yml` và cập nhật:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pokertrack
    username: postgres          # ← Đổi thành user PostgreSQL của bạn
    password: your_password     # ← Đổi thành password của bạn
```

---

## 4. Chạy Backend

```bash
cd backend
mvn spring-boot:run
```

Backend sẽ start tại: `http://localhost:8080`

---

## 5. Test API bằng curl / Postman

### Bước 1: Đăng nhập lấy token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"123456"}'
```

**Response mẫu:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { "id": 1, "email": "admin@test.com", "displayName": "Admin User" }
}
```

> ⚠️ **Copy token này** để dùng cho các request bên dưới (thay `<TOKEN>`).

---

### Bước 2: Xem thông tin user
```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <TOKEN>"
```

---

### Bước 3: Xem danh sách groups
```bash
curl http://localhost:8080/api/groups \
  -H "Authorization: Bearer <TOKEN>"
```

---

### Bước 4: Xem danh sách players
```bash
curl http://localhost:8080/api/groups/1/players \
  -H "Authorization: Bearer <TOKEN>"
```

---

### Bước 5: Tạo session cho hôm nay
```bash
curl -X POST http://localhost:8080/api/groups/1/sessions \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"sessionDate":"2026-03-23"}'
```

---

### Bước 6: Tạo entry cho session (playerId = 1)
> Lấy `sessionId` từ response Bước 5

```bash
curl -X POST http://localhost:8080/api/sessions/1/entries \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"playerId":1,"buyInTotal":500000,"cashOutTotal":750000}'
```

**Profit phải được tính server-side: `750000 - 500000 = 250000`**

---

### Bước 7: Xem danh sách entries (có join tên player)
```bash
curl http://localhost:8080/api/sessions/1/entries \
  -H "Authorization: Bearer <TOKEN>"
```

---

### Bước 8: Cập nhật settlement → PAID
```bash
curl -X POST http://localhost:8080/api/entries/1/settlement \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"status":"PAID"}'
```

---

### Bước 9: Test các lỗi validation

**9a. Settlement PARTIAL thiếu settledAmount → phải trả về 400:**
```bash
curl -X POST http://localhost:8080/api/entries/1/settlement \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"status":"PARTIAL"}'
```

**9b. Settlement DISPUTED thiếu settlementNote → phải trả về 400:**
```bash
curl -X POST http://localhost:8080/api/entries/1/settlement \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"status":"DISPUTED"}'
```

---

### Bước 10: Test lock session
```bash
# Lock session
curl -X POST http://localhost:8080/api/sessions/1/lock \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"locked":true}'

# Thử tạo entry khi locked (phải bị từ chối 422):
curl -X POST http://localhost:8080/api/sessions/1/entries \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"playerId":2,"buyInTotal":500000,"cashOutTotal":300000}'
```

---

## 6. Test Reports

```bash
# Tổng hợp
curl "http://localhost:8080/api/groups/1/reports/summary?from=2026-03-01&to=2026-03-31" \
  -H "Authorization: Bearer <TOKEN>"

# Theo player
curl "http://localhost:8080/api/groups/1/reports/by-player?from=2026-03-01&to=2026-03-31" \
  -H "Authorization: Bearer <TOKEN>"

# Theo ngày
curl "http://localhost:8080/api/groups/1/reports/by-day?from=2026-03-01&to=2026-03-31" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 7. Màn hình lỗi thường gặp

| Lỗi | Nguyên nhân | Cách sửa |
|-----|------------|----------|
| `Connection refused` | PostgreSQL chưa chạy | Start PostgreSQL service |
| `password authentication failed` | Sai user/password trong `application.yml` | Kiểm tra lại config |
| `relation "users" does not exist` | Chưa chạy `schema.sql` | Chạy lại Bước 2.2 |
| `401 Unauthorized` | Thiếu hoặc sai token | Đăng nhập lại Bước 5 |
| `error generating column profit` | PostgreSQL < 12 | Nâng cấp lên PostgreSQL 13+ |

---

## 8. Import vào Postman (optional)

Tạo một **Postman Collection** với base URL `http://localhost:8080` và environment variable:
- `TOKEN` = giá trị token lấy từ bước đăng nhập
- Header mặc định: `Authorization: Bearer {{TOKEN}}`
