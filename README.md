# Fingerprint Authentication Demo

Ứng dụng demo xác thực thiết bị sử dụng FingerprintJS Pro.

## Flow Xác Thực

### 1. Flow Tạo Token

```mermaid
sequenceDiagram
    participant Client
    participant Frontend
    participant Backend
    participant FingerprintSDK
    participant FingerprintServer

    Client->>Frontend: Truy cập trang web
    Note over Frontend: Khởi tạo FingerprintJS SDK
    Frontend->>FingerprintSDK: FingerprintJS.load()
    FingerprintSDK->>FingerprintServer: API call để lấy visitorId
    FingerprintServer-->>FingerprintSDK: Trả về visitorId và requestId
    FingerprintSDK-->>Frontend: Trả về kết quả qua SDK
    Note over Frontend: Lưu visitorId làm fingerprint
    Frontend->>Backend: POST /api/tokens
    Note over Frontend,Backend: Gửi kèm:
    Note over Frontend,Backend: - visitorId (fingerprint)
    Note over Frontend,Backend: - requestId (để verify)
    Note over Frontend,Backend: - timestamp
    Note over Frontend,Backend: - components
    Backend->>FingerprintServer: API call verify visitor
    Note over Backend,FingerprintServer: Gửi requestId để xác thực
    FingerprintServer-->>Backend: Kết quả xác thực
    Backend->>Backend: Kiểm tra bot detection
    Backend->>Backend: Tạo JWT token
    Note over Backend: Token chứa:
    Note over Backend: - visitorId (fingerprint)
    Note over Backend: - requestId
    Note over Backend: - timestamp
    Backend-->>Frontend: Trả về token
    Frontend->>Frontend: Lưu token vào localStorage
```

### 2. Flow Bảo Vệ API

```mermaid
sequenceDiagram
    participant Client
    participant Frontend
    participant Backend
    participant FingerprintSDK
    participant FingerprintServer

    Client->>Frontend: Gọi API (ví dụ: /api/stocks)
    Frontend->>Frontend: Lấy token từ localStorage
    Note over Frontend: Sử dụng FingerprintJS SDK
    Frontend->>FingerprintSDK: fp.get()
    FingerprintSDK->>FingerprintServer: API call để lấy requestId mới
    FingerprintServer-->>FingerprintSDK: Trả về requestId
    FingerprintSDK-->>Frontend: Trả về kết quả qua SDK
    Frontend->>Backend: Gửi request với token và requestId
    Note over Frontend,Backend: Headers:
    Note over Frontend,Backend: - X-App-Token: JWT token
    Note over Frontend,Backend: - X-Fingerprint: visitorId
    Note over Frontend,Backend: - X-Request-Id: requestId mới
    Backend->>Backend: Validate JWT token
    Note over Backend: Kiểm tra:
    Note over Backend: - Signature và expiration
    Note over Backend: - visitorId trong token
    Backend->>FingerprintServer: API call verify requestId
    Note over Backend,FingerprintServer: Gửi requestId để xác thực
    FingerprintServer-->>Backend: Kết quả verify
    alt Token và requestId hợp lệ
        Backend-->>Frontend: Trả về dữ liệu
        Frontend-->>Client: Hiển thị dữ liệu
    else Token hoặc requestId không hợp lệ
        Backend-->>Frontend: 401 Unauthorized
        Frontend->>Frontend: Xóa token cũ
        Frontend->>Frontend: Chuyển về trang login
    end
```

### 3. Flow Chống Bot và Tái Sử Dụng RequestId

```mermaid
sequenceDiagram
    participant Bot
    participant Backend
    participant FingerprintServer
    participant RealClient

    Note over Bot: Cố gắng tấn công bằng cách:
    Note over Bot: 1. Copy requestId từ request hợp lệ
    Note over Bot: 2. Sử dụng bot để tự động hóa request

    Bot->>Backend: Gửi request với requestId đã copy
    Note over Bot,Backend: Headers:
    Note over Bot,Backend: - X-App-Token: JWT token giả mạo
    Note over Bot,Backend: - X-Fingerprint: visitorId giả mạo
    Note over Bot,Backend: - X-Request-Id: requestId đã copy
    Backend->>FingerprintServer: Verify requestId
    Note over Backend,FingerprintServer: Kiểm tra:
    Note over Backend,FingerprintServer: 1. requestId đã được sử dụng
    Note over Backend,FingerprintServer: 2. Dấu hiệu bot (tốc độ request, pattern)
    Note over Backend,FingerprintServer: 3. Không khớp với visitorId
    FingerprintServer-->>Backend: Phát hiện bất thường
    Backend-->>Bot: 401 Unauthorized
    Note over Backend: Ghi log và chặn IP

    RealClient->>Backend: Request hợp lệ
    Backend->>FingerprintServer: Verify requestId mới
    FingerprintServer-->>Backend: Xác thực thành công
    Backend-->>RealClient: Trả về dữ liệu
```

## Cấu hình FingerprintJS

### Frontend (SDK)
```typescript
// Khởi tạo SDK
const fpPromise = FingerprintJS.load({
  apiKey: 'your-api-key',
  endpoint: 'https://api.fpjs.io', // hoặc proxy endpoint
  region: 'ap'
});

// Sử dụng SDK
const fp = await fpPromise;
const result = await fp.get();
// result.visitorId: định danh thiết bị
// result.requestId: mã xác thực request
```

### Backend (API)
```java
// Cấu hình trong application.properties
fingerprint.api.key=your-api-key
fingerprint.api.url=https://api.fpjs.io
fingerprint.api.region=ap

// Sử dụng API để verify
@PostMapping("/verify")
public ResponseEntity<?> verifyVisitor(
    @RequestParam String requestId,
    @RequestParam String visitorId
) {
    // Gọi API của Fingerprint Server để verify
    // Trả về kết quả xác thực
}
```

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
2. Cấu hình trong `application.properties`:
```properties
# JWT Configuration
jwt.secret=your-secret-key-here
jwt.expiration=300

# Security Configuration
app.security.max-failed-attempts=5
app.security.failed-attempt-window=3600

# Rate Limiting
app.rate.limit.window=3600
app.rate.limit.max=100

# Token Configuration
app.token.expiration=300
app.token.timestamp.tolerance=120
```

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
  - jjwt 0.12.5

## Bảo mật

- Xác thực và bảo mật:
  - Sử dụng FingerprintJS Pro để xác thực thiết bị
  - JWT token với HS256 và key size 256-bit
  - Kiểm tra fingerprint trong token validation
  - Rate limiting để tránh quá tải
  - Theo dõi và chặn các nỗ lực xác thực thất bại
  - Ghi log chi tiết cho các sự kiện bảo mật

- Cấu hình bảo mật:
  - API key được lưu trong biến môi trường
  - Hỗ trợ proxy để bảo vệ API key
  - Token expiration và timestamp validation
  - CORS được cấu hình chặt chẽ
  - Xử lý lỗi 401 và auto-refresh thông minh

## Giấy phép

MIT License