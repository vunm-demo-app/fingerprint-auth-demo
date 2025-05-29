# Local Development Setup

This guide explains how to set up the development environment for running the application locally.

## SSL Certificate Setup

For local development behind a corporate proxy (like FortiGate), you need to set up SSL certificates:

1. Create a `certs` directory in the project root:
```bash
mkdir certs
```

2. Copy your FortiGate CA certificate to the `certs` directory:
```bash
copy "PATH_TO_YOUR_CERTIFICATE\FortiGate CA.crt" certs\FortiGate_CA.crt
```

3. Import the certificate to your local Java keystore:
```bash
keytool -import -trustcacerts -keystore "PATH_TO_YOUR_JDK\lib\security\cacerts" -storepass changeit -noprompt -alias FortiGateCA2 -file "PATH_TO_YOUR_CERTIFICATE\FortiGate CA.crt"
```

Example:
```bash
keytool -import -trustcacerts -keystore "C:\Program Files\Eclipse Adoptium\jdk-21.0.2.13-hotspot\lib\security\cacerts" -storepass changeit -noprompt -alias FortiGateCA2 -file "C:\MyDocuments\VietCap\FortiGate CA.crt"
```

## Running the Application

After setting up the certificates:

1. Make sure Docker Desktop is running

2. Start the application using Docker Compose:
```bash
docker-compose up --build
```

The application will be available at:
- Frontend: http://localhost
- Backend API: http://localhost:8080

## Security Note

The certificate files are sensitive and should never be committed to version control. They are automatically ignored by .gitignore. 