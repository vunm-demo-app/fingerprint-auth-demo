import axios from 'axios';
import FingerprintJS, { GetResult } from '@fingerprintjs/fingerprintjs';
import SHA256 from 'crypto-js/sha256';

const fpPromise = FingerprintJS.load();

interface AppToken {
    token: string;
    fingerprint: string;
    expiresAt: number;
}

interface AppTokenRequest {
    deviceId: string;
    fingerprint: string;
    timestamp: number;
    components: Record<string, any>;
}

interface StockPrice {
    symbol: string;
    refPrice: number;
    ceilingPrice: number;
    floorPrice: number;
    matchPrice: number;
    change: number;
    changePercent: number;
    volume: number;
}

interface AdminLogParams {
    from?: string;
    to?: string;
    fingerprint?: string;
    ipAddress?: string;
    isSuspectedBot?: boolean;
    page?: number;
    size?: number;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

class ApiService {
    private token: AppToken | null = null;
    private fingerprint: string | null = null;
    private deviceId: string | null = null;
    private fingerprintComponents: Record<string, any> | null = null;
    private initPromise: Promise<void> | null = null;
    private fpResult: GetResult | null = null;
    private serverTimeDiff: number = 0;

    constructor() {
        // Try to restore fingerprint from localStorage
        const storedFingerprint = localStorage.getItem('fingerprint');
        const storedComponents = localStorage.getItem('fingerprintComponents');
        if (storedFingerprint && storedComponents) {
            try {
                this.fingerprint = storedFingerprint;
                this.fingerprintComponents = JSON.parse(storedComponents);
                console.log('Restored fingerprint from storage:', this.fingerprint);
            } catch (e) {
                console.error('Failed to restore fingerprint from storage:', e);
            }
        }
        
        // Initialize fingerprint and token when service is created
        this.initPromise = this.initialize();
    }

    private async getServerTime(): Promise<number> {
        try {
            const response = await api.get<{ serverTime: number }>('/time');
            const serverTime = response.data.serverTime;
            const localTime = Math.floor(Date.now() / 1000);
            this.serverTimeDiff = serverTime - localTime;
            console.log('Server time difference:', this.serverTimeDiff, 'seconds');
            return serverTime;
        } catch (error) {
            console.error('Failed to get server time:', error);
            return Math.floor(Date.now() / 1000);
        }
    }

    private getCurrentTimestamp(): number {
        return Math.floor(Date.now() / 1000) + this.serverTimeDiff;
    }

    private async initialize(): Promise<void> {
        try {
            // Get server time first
            await this.getServerTime();

            const { visitorId, components } = await this.getFingerprint();
            const deviceId = this.generateDeviceId();
            
            const request: AppTokenRequest = {
                deviceId,
                fingerprint: visitorId,
                timestamp: this.getCurrentTimestamp(),
                components
            };

            console.log('Sending token request:', {
                deviceId: request.deviceId,
                fingerprint: request.fingerprint,
                timestamp: request.timestamp,
                components: {
                    ...request.components,
                    canvas: request.components.canvas?.length,
                    audio: request.components.audio?.length,
                    fonts: request.components.fonts?.length
                }
            });

            const response = await api.post<AppToken>('/app-token', request);
            this.token = response.data;
            console.log('Token response:', this.token);
        } catch (error) {
            console.error('Failed to initialize ApiService:', error);
            throw error;
        }
    }

    private generateDeviceId(): string {
        if (!this.deviceId) {
            const storedDeviceId = localStorage.getItem('deviceId');
            if (storedDeviceId) {
                this.deviceId = storedDeviceId;
            } else {
                this.deviceId = 'web-' + Math.random().toString(36).substring(2, 15);
                localStorage.setItem('deviceId', this.deviceId);
            }
        }
        return this.deviceId;
    }

    private hashComponent(component: any): string {
        if (!component) return '';
        const str = typeof component === 'object' ? JSON.stringify(component) : String(component);
        return SHA256(str).toString();
    }

    private async getFingerprint(): Promise<{ visitorId: string, components: Record<string, any> }> {
        // If we have cached values and they're valid, use them
        if (this.fingerprint && this.fingerprintComponents) {
            console.log('Using cached fingerprint:', this.fingerprint);
            return {
                visitorId: this.fingerprint,
                components: this.fingerprintComponents
            };
        }
        
        const fp = await fpPromise;
        this.fpResult = await fp.get();
        
        this.fingerprint = this.fpResult.visitorId;
        console.log('Raw fingerprint components:', this.fpResult.components);

        // Get WebGL info safely
        const webglInfo = (this.fpResult.components as any).webgl || {};
        const webglSupported = !!webglInfo;
        const webglRenderer = webglInfo.renderer || '';
        const webglVendor = webglInfo.vendor || '';

        // Hash critical components first for debugging
        const fontsHash = this.hashComponent(this.fpResult.components.fonts);
        const audioHash = this.hashComponent(this.fpResult.components.audio);
        const canvasHash = this.hashComponent(this.fpResult.components.canvas);
        
        console.log('Hashed components:', {
            fonts: fontsHash,
            audio: audioHash,
            canvas: canvasHash
        });

        this.fingerprintComponents = {
            userAgent: navigator.userAgent,
            platform: navigator.platform,
            screenResolution: `${window.screen.width}x${window.screen.height}`,
            timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
            language: navigator.language,
            webglSupported,
            webglRenderer,
            webglVendor,
            cpuCores: navigator.hardwareConcurrency?.toString() || '',
            deviceMemory: (navigator as any).deviceMemory?.toString() || '',
            hardwareConcurrency: navigator.hardwareConcurrency?.toString() || '',
            touchSupport: 'ontouchstart' in window ? 'true' : 'false',
            colorDepth: window.screen.colorDepth?.toString() || '',
            pixelRatio: window.devicePixelRatio?.toString() || '',
            fonts: fontsHash,
            audio: audioHash,
            canvas: canvasHash
        };

        // Store fingerprint in localStorage
        try {
            localStorage.setItem('fingerprint', this.fingerprint);
            localStorage.setItem('fingerprintComponents', JSON.stringify(this.fingerprintComponents));
            console.log('Stored fingerprint in localStorage');
        } catch (e) {
            console.error('Failed to store fingerprint in localStorage:', e);
        }

        console.log('Final fingerprint components:', this.fingerprintComponents);

        return {
            visitorId: this.fingerprint,
            components: this.fingerprintComponents
        };
    }

    private async ensureToken(): Promise<void> {
        // Wait for initialization to complete if it's still ongoing
        if (this.initPromise) {
            await this.initPromise;
            this.initPromise = null;
        }

        // Check if token needs refresh
        if (!this.token || Date.now() >= this.token.expiresAt * 1000) {
            console.log('Token needs refresh. Current token:', this.token);
            const { visitorId, components } = await this.getFingerprint();
            const deviceId = this.generateDeviceId();
            
            const request: AppTokenRequest = {
                deviceId,
                fingerprint: visitorId,
                timestamp: this.getCurrentTimestamp(),
                components
            };

            console.log('Sending token refresh request:', {
                deviceId: request.deviceId,
                fingerprint: request.fingerprint,
                timestamp: request.timestamp,
                components: {
                    ...request.components,
                    canvas: request.components.canvas?.length,
                    audio: request.components.audio?.length,
                    fonts: request.components.fonts?.length
                }
            });

            const response = await api.post<AppToken>('/app-token', request);
            this.token = response.data;
            console.log('Token refresh response:', this.token);
        }
    }

    private getHeaders() {
        if (!this.fingerprint || !this.token) {
            throw new Error('Fingerprint or token not initialized');
        }
        return {
            'X-Fingerprint': this.fingerprint,
            'X-App-Token': this.token.token
        };
    }

    async getAllStocks(): Promise<StockPrice[]> {
        await this.ensureToken();
        const response = await api.get<StockPrice[]>('/stock-prices', {
            headers: this.getHeaders()
        });
        return response.data;
    }

    async getStock(symbol: string): Promise<StockPrice> {
        await this.ensureToken();
        const response = await api.get<StockPrice>(`/stock-prices/${symbol}`, {
            headers: this.getHeaders()
        });
        return response.data;
    }

    async getAdminStats(from: string, to: string) {
        await this.ensureToken();
        return api.get('/admin/statistics', {
            params: { from, to },
            headers: this.getHeaders()
        });
    }

    async getAdminLogs(params: AdminLogParams) {
        await this.ensureToken();
        return api.get('/admin/logs', {
            params,
            headers: this.getHeaders()
        });
    }

    async getIpFingerprintCorrelation() {
        await this.ensureToken();
        return api.get('/admin/correlation', {
            headers: this.getHeaders()
        });
    }
}

export const apiService = new ApiService();
export type { StockPrice, AppToken, AppTokenRequest }; 