# Changelog

All notable changes to this project will be documented in this file.

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

### Security
- Added detailed logging for security-related events
  - Failed attempts tracking
  - IP-Fingerprint correlation analysis
  - Rate limiting information
  - Token validation failures
- Improved timestamp validation accuracy through server synchronization

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