FROM golang:1.21-alpine AS builder

WORKDIR /app

# Copy go.mod and go.sum files
COPY go.mod ./
COPY go.sum ./

# Download dependencies
RUN go mod download

# Copy source code
COPY . .

# Build the application
RUN CGO_ENABLED=1 GOOS=linux go build -a -installsuffix cgo -o app .

# Final stage
FROM alpine:latest

# Install required packages
RUN apk --no-cache add ca-certificates tzdata sqlite

WORKDIR /app

# Copy the binary from the builder stage
COPY --from=builder /app/app .

# Create data directory
RUN mkdir -p /app/data

# Expose port
EXPOSE 8080

# Command to run the application
CMD ["./app"]