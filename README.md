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
    Backend->>Backend: Kiểm tra requestId đã sử dụng
    alt requestId đã sử dụng
        Backend-->>Frontend: 401 Unauthorized
        Note over Backend: Ghi log và chặn IP
    else requestId chưa sử dụng
        Backend->>FingerprintServer: Verify requestId
        FingerprintServer-->>Backend: Kết quả verify
        alt Verify thành công
            Backend->>Backend: Lưu requestId đã sử dụng
            Backend-->>Frontend: Trả về dữ liệu
        else Verify thất bại
            Backend-->>Frontend: 401 Unauthorized
        end
    end
```

### 3. Flow Chống Bot và Tái Sử Dụng RequestId

```mermaid
sequenceDiagram
    participant Attacker
    participant Backend
    participant FingerprintServer
    participant RealClient

    Note over Attacker: Cố gắng tấn công bằng cách:
    Note over Attacker: 1. Copy requestId từ request hợp lệ
    Note over Attacker: 2. Sử dụng lại requestId để tạo token mới

    Attacker->>Backend: Gửi request với requestId đã copy
    Note over Attacker,Backend: Headers:
    Note over Attacker,Backend: - X-App-Token: JWT token giả mạo
    Note over Attacker,Backend: - X-Fingerprint: visitorId giả mạo
    Note over Attacker,Backend: - X-Request-Id: requestId đã copy
    Backend->>Backend: Kiểm tra requestId đã sử dụng
    Note over Backend: requestId đã được dùng để tạo token trước đó
    Backend-->>Attacker: 401 Unauthorized
    Note over Backend: Ghi log và chặn IP

    RealClient->>Backend: Request hợp lệ với requestId mới
    Backend->>Backend: Kiểm tra requestId chưa sử dụng
    Note over Backend: requestId hợp lệ và chưa được sử dụng
    Backend->>FingerprintServer: Verify requestId
    FingerprintServer-->>Backend: Xác thực thành công
    Backend->>Backend: Lưu requestId đã sử dụng
    Backend-->>RealClient: Tạo token thành công
```

### 4. Flow Phân Tích Lỗ Hổng và Cách Bảo Vệ

```mermaid
sequenceDiagram
    participant Attacker
    participant Postman
    participant Backend
    participant FingerprintServer
    participant RealBrowser

    Note over Attacker: Phân tích request từ browser:
    Note over Attacker: 1. Copy toàn bộ headers
    Note over Attacker: 2. Copy requestId
    Note over Attacker: 3. Copy JWT token
    Note over Attacker: 4. Copy visitorId

    Attacker->>Postman: Tạo request mới
    Note over Attacker,Postman: Headers giống hệt:
    Note over Attacker,Postman: - X-App-Token: JWT token đã copy
    Note over Attacker,Postman: - X-Fingerprint: visitorId đã copy
    Note over Attacker,Postman: - X-Request-Id: requestId đã copy
    Postman->>Backend: Gửi request
    Backend->>FingerprintServer: Verify requestId
    Note over Backend,FingerprintServer: Vấn đề:
    Note over Backend,FingerprintServer: 1. requestId vẫn còn hiệu lực
    Note over Backend,FingerprintServer: 2. Không có thông tin browser
    Note over Backend,FingerprintServer: 3. Không có components thật
    FingerprintServer-->>Backend: Verify thành công
    Note over Backend: Lỗ hổng bảo mật!
    Backend-->>Postman: 200 OK

    Note over RealBrowser: Cách bảo vệ đúng:
    Note over RealBrowser: 1. Luôn yêu cầu components mới
    Note over RealBrowser: 2. Verify browser environment
    Note over RealBrowser: 3. Kiểm tra tính nhất quán
    Note over RealBrowser: 4. Sử dụng proxy endpoint

    RealBrowser->>Backend: Request hợp lệ
    Note over RealBrowser,Backend: Headers:
    Note over RealBrowser,Backend: - Components mới từ SDK
    Note over RealBrowser,Backend: - Browser environment
    Note over RealBrowser,Backend: - requestId mới
    Backend->>FingerprintServer: Verify với components
    Note over Backend,FingerprintServer: Kiểm tra:
    Note over Backend,FingerprintServer: 1. Components có hợp lệ
    Note over Backend,FingerprintServer: 2. Browser environment
    Note over Backend,FingerprintServer: 3. Tính nhất quán
    FingerprintServer-->>Backend: Xác thực thành công
    Backend-->>RealBrowser: Trả về dữ liệu
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
  - **Mỗi requestId chỉ được sử dụng một lần để tạo token**
  - **Lưu trữ và kiểm tra các requestId đã sử dụng**
  - **Tự động chặn các request sử dụng requestId đã dùng**

## Giấy phép

MIT License