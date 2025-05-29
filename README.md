# Fingerprint Authentication Demo

A demo application showcasing fingerprint-based authentication with VietCap-like trading interface.

## Overview

This project demonstrates a secure authentication system using browser fingerprinting technology, combined with a real-time stock trading interface inspired by VietCap's design.

### Key Features

- **Secure Authentication**
  - Browser fingerprinting with FingerprintJS
  - JWT-based token system
  - Multi-layer bot protection
  - IP and device tracking

- **Trading Interface**
  - Real-time stock price updates
  - VietCap-inspired design
  - Responsive data grid
  - Price change visualization

- **Admin Dashboard**
  - IP-Fingerprint correlation analysis
  - Real-time monitoring
  - Traffic analytics
  - Security event logging

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.5.0
- JWT Authentication
- H2 Database
- SLF4J & Logback

### Frontend
- React 18
- TypeScript
- Ant Design
- FingerprintJS
- Vite

## Getting Started

### Prerequisites
- Java 21+
- Node.js 20+
- npm 9+
- Docker (optional)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/fingerprint-auth-demo.git
   cd fingerprint-auth-demo
   ```

2. **Backend Setup**
   ```bash
   cd fingerprint-auth-demo-backend
   ./mvnw spring-boot:run
   ```

3. **Frontend Setup**
   ```bash
   cd fingerprint-auth-demo-frontend
   npm install
   npm run dev
   ```

### Docker Setup
```bash
docker-compose up -d
```

## Configuration

### Backend (application.yml)
```yaml
app:
  token:
    expiration: 300
    timestamp:
      tolerance: 30
  rate:
    limit:
      window: 3600
      max: 100
```

### Frontend (.env)
```
VITE_API_BASE_URL=http://localhost:8080/api
```

## Security Features

1. **Device Fingerprinting**
   - Hardware and browser characteristics tracking
   - Component consistency verification
   - Bot pattern detection

2. **Request Protection**
   - Rate limiting per fingerprint
   - Timestamp validation
   - IP diversity monitoring

3. **Token Security**
   - Short-lived JWT tokens
   - Device binding
   - Secure token storage

## Documentation

- [Release Notes](RELEASE_NOTES.md)
- [API Documentation](docs/API.md)
- [Security Guide](docs/SECURITY.md)
- [Development Guide](docs/DEVELOPMENT.md)

## License

MIT License - See [LICENSE](LICENSE) for details 