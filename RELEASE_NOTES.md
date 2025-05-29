# Release Notes

## Version 0.2.0 (2025-05-29)

### Security Enhancements
- Enhanced token security with device binding
- Added timestamp validation to prevent replay attacks
- Implemented device ID tracking and validation
- Added IP and User-Agent tracking in token claims

### API Changes
- Changed token endpoint from `/api/token` to `/api/app-token`
- Updated token request format to include deviceId and timestamp
- Enhanced token response with more security information
- Added validation for request parameters

### Frontend Updates
- Added automatic device ID generation and storage
- Updated API service to handle new token format
- Improved error handling and user feedback
- Added TypeScript interfaces for new request/response types

### Backend Improvements
- Added comprehensive logging with SLF4J
- Enhanced validation in TokenService
- Added new AppTokenRequest model
- Improved error responses with proper HTTP status codes

### Development
- Updated documentation with new API formats
- Added security features documentation
- Enhanced setup instructions
- Added environment variable documentation

## Version 0.1.0 (2025-05-29)

### Initial Release
- Basic fingerprint-based authentication
- Real-time stock price monitoring
- VietCap-style trading interface
- JWT-based token authentication
- Frontend built with React and Ant Design
- Backend built with Spring Boot
- Docker support for deployment

## Version 0.0.3 (2025-05-29)

### Admin Dashboard
- Added IP-Fingerprint correlation visualization
- Enhanced data analytics and reporting
- Improved dashboard layout and responsiveness
- Added real-time monitoring features

### UI/UX Updates
- Streamlined admin interface
- Removed redundant status column
- Enhanced data presentation
- Improved user interaction feedback

### Performance
- Optimized backend request handling
- Improved data caching mechanisms
- Enhanced error handling and logging
- Updated system documentation

## Version 0.0.2 (2025-05-29)

### Changed
- Updated Spring Boot version from 3.2.3 to 3.5.0
- Updated Bucket4j version to 8.0.1
- Renamed project folders for better clarity:
  - `backend` → `fingerprint-auth-demo-backend`
  - `frontend` → `fingerprint-auth-demo-frontend`
- Removed Maven wrapper in favor of local Maven installation
- Updated Docker configurations for better build process

## Version 0.0.1 (2025-05-29)

### Added
- Initial project setup with Spring Boot backend and React frontend
- Browser fingerprinting implementation using FingerprintJS
- Token-based authentication system with short-lived tokens
- Rate limiting based on browser fingerprint (10 requests per minute)
- Real-time stock price simulation with random data
- Modern UI using Material-UI with dark theme
- Stock price table with auto-refresh every 5 seconds
- Docker support with multi-stage builds
- Docker Compose configuration for local development

### Technical Details
#### Backend
- Spring Boot 3.2.3 with Java 21
- DDD and Hexagonal Architecture implementation
- H2 in-memory database
- JWT for token generation and validation
- Bucket4j for rate limiting
- REST API with proper error handling
- CORS configuration for frontend access

#### Frontend
- React 18 with TypeScript
- Vite for fast development and building
- Material-UI for component library
- FingerprintJS for browser fingerprinting
- Axios for API communication
- Automatic token management
- Error handling and loading states
- Responsive design

### Infrastructure
- Multi-stage Dockerfile for both frontend and backend
- Nginx configuration for frontend serving and API proxying
- Docker Compose setup for local development
- Ready for deployment on render.com