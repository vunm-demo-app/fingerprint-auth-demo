# Fingerprint Authentication Demo

Dự án này minh họa cách sử dụng FingerprintJS Pro để phát hiện bot và xác thực người dùng thông qua browser fingerprinting.

## Tổng quan

Ứng dụng bao gồm:

- **Backend**: Ứng dụng Spring Boot xác minh fingerprint và phát hiện bot
- **Frontend**: Ứng dụng React sử dụng FingerprintJS Pro để tạo fingerprint

## Tính năng

- Browser fingerprinting với FingerprintJS Pro
- Phát hiện bot phía server
- Giới hạn tốc độ truy cập dựa trên fingerprint
- Kiểm tra tính nhất quán của fingerprint
- Bảng điều khiển admin để giám sát hoạt động đáng ngờ

## Cài đặt

### Yêu cầu

- Java 17+
- Node.js 16+
- Docker (tùy chọn)

### Biến môi trường

#### Backend (.env file trong thư mục fingerprint-auth-demo-backend)

```
FINGERPRINT_API_KEY=your_secret_api_key
FINGERPRINT_API_URL=https://api.fpjs.io
FINGERPRINT_API_REGION=us
```

#### Frontend (.env file trong thư mục fingerprint-auth-demo-frontend)

```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_FINGERPRINT_PUBLIC_API_KEY=zThPOeeB10e17zhjQbbh
```

### Chạy trên môi trường local

1. Khởi động backend:

```bash
cd fingerprint-auth-demo-backend
./mvnw spring-boot:run
```

2. Khởi động frontend:

```bash
cd fingerprint-auth-demo-frontend
npm install
npm run dev
```

### Chạy với Docker

```bash
docker-compose up
```

## API Endpoints

- `POST /api/app-token`: Tạo token ứng dụng với xác minh fingerprint
- `GET /api/stock-prices`: Lấy giá cổ phiếu (endpoint được bảo vệ)
- `GET /api/admin/logs`: Lấy nhật ký yêu cầu (endpoint admin)
- `GET /api/admin/statistics`: Lấy thống kê (endpoint admin)

## Phát hiện Bot

Ứng dụng sử dụng nhiều lớp phát hiện bot:

1. **Phía client**: Phát hiện bot của FingerprintJS Pro
2. **Phía server**: Xác minh sử dụng FingerprintJS Pro API
3. **Logic tùy chỉnh**: Kiểm tra bổ sung cho các mẫu đáng ngờ

## Cân nhắc bảo mật

- API key nên được giữ an toàn và không commit vào version control
- Giới hạn tốc độ được triển khai để ngăn chặn lạm dụng
- Tính nhất quán của fingerprint được kiểm tra để phát hiện giả mạo

## Cách FingerprintJS Pro hoạt động

FingerprintJS Pro cung cấp khả năng phát hiện bot mạnh mẽ hơn so với phiên bản miễn phí:

1. **Phát hiện bot nâng cao**: Sử dụng machine learning để phát hiện bot với độ chính xác cao
2. **Xác minh phía server**: API cho phép xác minh fingerprint ở phía server
3. **Phân tích hành vi**: Phát hiện các mẫu hành vi bất thường
4. **Báo cáo chi tiết**: Cung cấp thông tin về loại bot và xác suất

## Giấy phép

Dự án này chỉ dành cho mục đích demo.