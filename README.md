# Fingerprint Authentication Demo

Ứng dụng demo xác thực thiết bị sử dụng FingerprintJS Pro.

## Tính năng

- Xác thực thiết bị thông qua FingerprintJS Pro
- Hiển thị thông tin chi tiết về thiết bị truy cập:
  - Mã định danh thiết bị
  - Thông tin cơ bản (IP, trình duyệt, hệ điều hành)
  - Lịch sử truy cập
  - Độ tin cậy của xác thực
  - Thông tin yêu cầu
- Giao diện người dùng thân thiện với Ant Design
- Hỗ trợ đa ngôn ngữ (Tiếng Việt)
- Responsive design

## Cấu trúc dự án

```
fingerprint-auth-demo/
├── fingerprint-auth-demo-frontend/     # Frontend React + Vite
│   ├── src/
│   │   ├── components/                 # React components
│   │   │   ├── VisitorInfo.tsx        # Component hiển thị thông tin thiết bị
│   │   │   ├── ErrorPage.tsx          # Component xử lý lỗi
│   │   │   └── StockTable.tsx         # Component hiển thị dữ liệu chứng khoán
│   │   ├── services/                  # API services
│   │   │   └── api.ts                 # Service xử lý API calls
│   │   └── App.tsx                    # Component chính
│   └── .env                           # Cấu hình môi trường
└── fingerprint-auth-demo-backend/      # Backend Spring Boot
    └── src/
        └── main/
            ├── java/                  # Java source code
            └── resources/             # Cấu hình backend
```

## Cài đặt và Chạy

### Frontend

1. Cài đặt dependencies:
```bash
cd fingerprint-auth-demo-frontend
npm install
```

2. Cấu hình môi trường:
- Tạo file `.env` từ `.env.example`
- Cập nhật các biến môi trường:
  - `VITE_FINGERPRINT_API_KEY`: API key của FingerprintJS Pro
  - `VITE_FINGERPRINT_API_URL`: URL API của FingerprintJS Pro
  - `VITE_USE_PROXY`: Sử dụng proxy hay không (true/false)
  - `VITE_API_BASE_URL`: URL của backend API

3. Chạy development server:
```bash
npm run dev
```

### Backend

1. Cài đặt Java 17 hoặc cao hơn
2. Cấu hình trong `application.properties`
3. Chạy ứng dụng:
```bash
./mvnw spring-boot:run
```

## API Endpoints

### Frontend
- `/`: Trang chủ hiển thị thông tin thiết bị
- `/admin`: Trang quản trị (yêu cầu xác thực)

### Backend
- `/api/fpjs`: Proxy endpoint cho FingerprintJS Pro
- `/api/stocks`: API lấy dữ liệu chứng khoán
- `/api/tokens`: API quản lý token

## Công nghệ sử dụng

- Frontend:
  - React 18
  - Vite
  - Ant Design
  - FingerprintJS Pro
  - TypeScript
  - Styled Components

- Backend:
  - Spring Boot
  - Java 17
  - Maven

## Bảo mật

- Sử dụng FingerprintJS Pro để xác thực thiết bị
- API key được lưu trong biến môi trường
- Hỗ trợ proxy để bảo vệ API key
- Rate limiting để tránh quá tải

## Giấy phép

MIT License