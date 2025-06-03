# Fingerprint Authentication Demo - Frontend

This is the frontend part of the Fingerprint Authentication Demo project, built with React, TypeScript, and Ant Design.

## Setup

1. Clone the repository
2. Copy `.env.example` to `.env`
3. Update the `.env` file with your FingerprintJS Pro API key:
   ```
   VITE_FINGERPRINT_PUBLIC_API_KEY=your_fingerprint_public_api_key_here
   ```
4. Install dependencies:
   ```bash
   npm install
   ```
5. Start the development server:
   ```bash
   npm run dev
   ```

## Environment Variables

- `VITE_API_BASE_URL`: URL of the backend API (default: http://localhost:8080/api)
- `VITE_FINGERPRINT_PUBLIC_API_KEY`: Your FingerprintJS Pro public API key

## Security Notes

- Never commit your `.env` file to version control
- Add `.env` to your `.gitignore` file
- Use `.env.example` as a template for required environment variables

## Building for Production

```bash
npm run build
```

The build artifacts will be stored in the `dist/` directory.