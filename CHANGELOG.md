# Changelog

Tất cả các thay đổi đáng chú ý trong dự án này sẽ được ghi lại trong file này.

## [1.1.0] - 2024-03-06

### Thay đổi
- Cập nhật giao diện VisitorInfo component
  - Thêm styled components cho giao diện đẹp hơn
  - Hiển thị đầy đủ thông tin từ FingerprintJS Pro API
  - Thêm các icon trực quan cho từng mục thông tin
  - Dịch các label sang tiếng Việt
- Cải thiện cấu trúc dữ liệu
  - Định nghĩa rõ ràng interface cho visitor info
  - Loại bỏ các trường không sử dụng
  - Tối ưu hóa hiển thị thông tin

### Sửa lỗi
- Sửa lỗi icon BrowserOutlined không tồn tại
- Sửa lỗi hiển thị thông tin trống
- Sửa lỗi định dạng ngày tháng

## [1.0.0] - 2024-03-05

### Thêm mới
- Khởi tạo dự án với React + Vite
- Tích hợp FingerprintJS Pro
- Tạo các component cơ bản:
  - VisitorInfo: Hiển thị thông tin thiết bị
  - ErrorPage: Xử lý lỗi
  - StockTable: Hiển thị dữ liệu chứng khoán
- Cấu hình proxy cho FingerprintJS Pro
- Thêm biến môi trường cho cấu hình
- Tích hợp Ant Design cho UI
- Hỗ trợ đa ngôn ngữ (Tiếng Việt)

### Cấu hình
- Thêm các biến môi trường:
  - VITE_FINGERPRINT_API_KEY
  - VITE_FINGERPRINT_API_URL
  - VITE_USE_PROXY
  - VITE_API_BASE_URL
- Cấu hình TypeScript
- Cấu hình ESLint và Prettier
- Thêm các script npm

### Tài liệu
- Tạo README.md với hướng dẫn cài đặt và sử dụng
- Tạo CHANGELOG.md để theo dõi thay đổi
- Thêm comments cho code
- Cập nhật cấu trúc thư mục

## [0.2.0] - 2025-05-30

### Added
- Added Go backend implementation with Gin framework
  - Created SQLite database integration with pure Go driver
  - Implemented all API endpoints from Java version
  - Added middleware for authentication and CORS
  - Created services for fingerprint verification and token management
  - Added Docker configuration for Go backend

### Changed
- Updated docker-compose.yml to use Go backend instead of Java
- Updated render.yaml for deployment with Go backend
- Improved performance with lightweight Go implementation
- Reduced resource usage for better deployment on free tier services

## [0.1.0] - 2025-05-30

### Added
- Upgraded to FingerprintJS Pro from free version
  - Added server-side bot detection using FingerprintJS Pro API
  - Integrated bot probability and type detection
  - Added bot detection fields to FingerprintDetails model
- Created FingerprintApiService for interacting with FingerprintJS Pro API
- Added FingerprintConfig for API configuration
- Added environment variables support for FingerprintJS Pro API
- Enhanced TypeScript types for FingerprintJS Pro integration

### Changed
- Updated frontend to use FingerprintJS Pro client SDK
- Modified FingerprintVerificationService to use FingerprintJS Pro API
- Removed IP-to-fingerprint correlation checks (replaced by Pro bot detection)
- Updated TokenService to rely on FingerprintJS Pro bot detection
- Improved error handling and null safety in TypeScript code
- Updated documentation with FingerprintJS Pro setup instructions

### Security
- Enhanced bot detection with professional-grade FingerprintJS Pro
- Improved accuracy of bot identification with server-side verification
- Added bot probability scoring for more nuanced bot handling

## [0.0.4] - 2024-03-21

### Added
- Enhanced logging for token generation and validation
  - Added detailed IP address logging
  - Added User-Agent logging
  - Added Device ID logging
  - Added component information in fingerprint validation logs
  - Added timestamp validation details with time differences
- Increased timestamp tolerance to 120 seconds (2 minutes) for better handling of time synchronization issues
- Added server time synchronization feature
  - New `/api/time` endpoint to provide server time
  - Client-side time difference calculation and adjustment
  - Automatic timestamp synchronization for all requests

### Changed
- Improved token validation logging with specific failure reasons
- Enhanced security logging with more detailed information about suspicious activities
- Updated logging format for better readability and debugging
- Modified timestamp handling to use server-synchronized time
- Improved UI/UX
  - Restored trading interface as main demo page
  - Removed admin route from navigation
  - Made admin dashboard accessible only via direct URL
  - Enhanced layout and styling

### Security
- Added detailed logging for security-related events
  - Failed attempts tracking
  - IP-Fingerprint correlation analysis
  - Rate limiting information
  - Token validation failures
- Improved timestamp validation accuracy through server synchronization
- Enhanced admin access security by hiding admin route from UI

## [0.0.3] - Previous Release

### Added
- Docker optimization
  - Added Maven dependency caching
  - Added VOLUME for .m2 repository persistence
  - Improved layer caching
- Render.com deployment configuration
  - Added render.yaml
  - Configured disk persistence for Maven cache
  - Added health check endpoint

## [0.0.2] - Previous Release

### Added
- CORS configuration
  - Centralized CORS configuration using WebConfig
  - Environment variable support for CORS origins
- Port configuration
  - Dynamic PORT from environment variables
  - Updated application.yml configuration

## [0.0.1] - Initial Release

### Added
- Initial project setup
- Basic fingerprint authentication
- Token generation and validation
- Rate limiting implementation
- Basic security measures

## [1.2.0] - 2024-03-07

### Thay đổi
- Cải thiện xử lý JWT token
  - Chuyển từ HS512 sang HS256 để tối ưu hiệu suất
  - Sử dụng `Jwts.SIG.HS256.key()` để tạo key an toàn
  - Cập nhật API của jjwt lên phiên bản 0.12.5
  - Thống nhất secret key giữa các service
- Cải thiện bảo mật
  - Thêm kiểm tra fingerprint trong token validation
  - Ghi log chi tiết cho các lỗi xác thực
  - Tăng cường xử lý lỗi và logging
- Cải thiện frontend
  - Thêm xử lý 401 error trong StockTable
  - Dừng auto-refresh sau 5 lần lỗi 401 liên tiếp
  - Hiển thị thông báo lỗi rõ ràng hơn

### Sửa lỗi
- Sửa lỗi token validation không khớp do secret key khác nhau
- Sửa lỗi key size không đủ cho HS512
- Sửa lỗi frontend bị loop refresh khi token hết hạn 