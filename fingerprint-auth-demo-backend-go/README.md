# Fingerprint Authentication Demo Backend (Go)

Backend API cho ứng dụng demo xác thực bằng fingerprint, được viết bằng Go với Gin framework và SQLite.

## Tính năng

- Xác thực người dùng bằng fingerprint
- Phát hiện bot với FingerprintJS Pro
- Giới hạn tốc độ truy cập
- Kiểm tra tính nhất quán của fingerprint
- API cho bảng điều khiển admin

## Yêu cầu

- Go 1.21+
- SQLite

## Cài đặt

1. Clone repository:

```bash
git clone https://github.com/yourusername/fingerprint-auth-demo.git
cd fingerprint-auth-demo/fingerprint-auth-demo-backend-go
```

2. Cài đặt dependencies:

```bash
go mod download
```

3. Tạo file `.env` với nội dung:

```
PORT=8080
GIN_MODE=debug
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://fingerprint-auth-demo.onrender.com
FINGERPRINT_API_KEY=your_secret_api_key
FINGERPRINT_API_URL=https://api.fpjs.io
FINGERPRINT_API_REGION=us
```

4. Chạy ứng dụng:

```bash
go run main.go
```

## API Endpoints

- `GET /health`: Kiểm tra trạng thái hoạt động
- `GET /api/time`: Lấy thời gian hiện tại của server
- `POST /api/app-token`: Tạo token ứng dụng với xác minh fingerprint
- `GET /api/stock-prices`: Lấy danh sách giá cổ phiếu (yêu cầu xác thực)
- `GET /api/stock-prices/:symbol`: Lấy giá cổ phiếu theo mã (yêu cầu xác thực)
- `GET /api/admin/statistics`: Lấy thống kê (yêu cầu xác thực)
- `GET /api/admin/logs`: Lấy nhật ký yêu cầu (yêu cầu xác thực)
- `GET /api/admin/correlation`: Lấy tương quan giữa IP và fingerprint (yêu cầu xác thực)

## Docker

Để chạy ứng dụng với Docker:

```bash
docker build -t fingerprint-auth-backend-go .
docker run -p 8080:8080 -e FINGERPRINT_API_KEY=your_secret_api_key fingerprint-auth-backend-go
```

## Cấu trúc dự án

- `config/`: Cấu hình ứng dụng
- `controllers/`: Xử lý yêu cầu HTTP
- `database/`: Kết nối và khởi tạo cơ sở dữ liệu
- `middleware/`: Middleware cho Gin
- `models/`: Định nghĩa cấu trúc dữ liệu
- `services/`: Logic nghiệp vụ
- `utils/`: Tiện ích