/// <reference types="vite/client" />

interface ImportMetaEnv {
    readonly VITE_API_BASE_URL: string
    readonly VITE_FINGERPRINT_PUBLIC_KEY: string
    readonly VITE_FINGERPRINT_PROXY_BASE_URL: string
    readonly VITE_FINGERPRINT_USE_PROXY: string // "true" or "false"
    readonly VITE_FINGERPRINT_API_URL: string
    readonly VITE_FINGERPRINT_USE_CUSTOM_ENDPOINT: string // "true" or "false"
}

interface ImportMeta {
    readonly env: ImportMetaEnv
} 