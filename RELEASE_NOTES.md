# Release Notes - v0.1.0 (30/05/2025)

## Nâng cấp lên FingerprintJS Pro

Phiên bản 0.1.0 đánh dấu một bước tiến quan trọng với việc nâng cấp từ FingerprintJS miễn phí lên phiên bản Pro. Điều này mang lại nhiều cải tiến đáng kể về khả năng phát hiện bot và bảo mật.

### Tính năng mới

1. **Phát hiện bot nâng cao**
   - Sử dụng FingerprintJS Pro API để phát hiện bot với độ chính xác cao
   - Phân tích xác suất bot và phân loại loại bot
   - Tích hợp thông tin phát hiện bot vào quy trình xác thực

2. **Xác minh phía server**
   - Thêm FingerprintApiService để gọi API FingerprintJS Pro
   - Xác minh fingerprint ở phía server để tăng cường bảo mật
   - Lưu trữ thông tin phát hiện bot trong cơ sở dữ liệu

3. **Cấu hình linh hoạt**
   - Thêm biến môi trường cho cấu hình FingerprintJS Pro
   - Hỗ trợ cấu hình API key, URL và region
   - Tách biệt cấu hình giữa môi trường phát triển và sản xuất

### Cải tiến

1. **Loại bỏ kiểm tra IP-fingerprint**
   - Thay thế kiểm tra IP-fingerprint bằng phát hiện bot của FingerprintJS Pro
   - Giảm false positive cho người dùng hợp pháp sử dụng nhiều IP

2. **Cải thiện trải nghiệm người dùng**
   - Giảm số lượng từ chối truy cập sai cho người dùng hợp pháp
   - Tăng độ chính xác trong việc phát hiện bot thực sự

3. **Tối ưu hóa mã nguồn**
   - Cập nhật TypeScript types cho FingerprintJS Pro
   - Cải thiện xử lý lỗi và an toàn null
   - Tái cấu trúc mã để tận dụng tối đa API FingerprintJS Pro

### Hướng dẫn nâng cấp

1. **Cấu hình API key**
   - Thêm API key bí mật vào file .env trong thư mục backend
   - Sử dụng API key công khai trong frontend

2. **Cài đặt thư viện**
   - Cài đặt @fingerprintjs/fingerprintjs-pro thay vì phiên bản miễn phí

3. **Kiểm tra tích hợp**
   - Kiểm tra phát hiện bot trong bảng điều khiển admin
   - Xác minh rằng thông tin bot được hiển thị chính xác

### Lưu ý

- API key nên được giữ bí mật và không commit vào version control
- Phiên bản Pro yêu cầu kết nối internet để gọi API FingerprintJS
- Đảm bảo cấu hình CORS cho phép gọi API từ domain của bạn