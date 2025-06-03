import React, { useEffect, useState, useRef } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout, Spin, Row, Col } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';
import StockTable from './components/StockTable';
import AdminDashboard from './pages/AdminDashboard';
import ErrorPage from './components/ErrorPage';
import { apiService, ApiServiceWithPublicMethods } from './services/api';
import FingerprintJS from '@fingerprintjs/fingerprintjs-pro';
import VisitorInfo from './components/VisitorInfo';

// Add IpLocation interface
interface IpLocation {
  ip?: string;
  city?: { name?: string };
  subdivisions?: { name?: string }[];
  country?: { name?: string };
  timezone?: string;
  continent?: { name?: string };
  tor?: boolean;
  vpn?: boolean;
  proxy?: boolean;
  datacenter?: boolean;
}

const { Content } = Layout;

const FINGERPRINT_PROXY_BASE_URL = import.meta.env.VITE_FINGERPRINT_PROXY_BASE_URL || 'http://localhost:8080';
const USE_PROXY = (import.meta.env.VITE_FINGERPRINT_USE_PROXY || 'false').toLowerCase() === 'true';
const USE_CDN = (import.meta.env.VITE_FINGERPRINT_USE_CDN || 'false').toLowerCase() === 'true';
const PUBLIC_KEY = import.meta.env.VITE_FINGERPRINT_PUBLIC_KEY || '';
const FINGERPRINT_API_URL = import.meta.env.VITE_FINGERPRINT_API_URL || 'https://api.fpjs.io';
const USE_CUSTOM_ENDPOINT = (import.meta.env.VITE_FINGERPRINT_USE_CUSTOM_ENDPOINT || 'false').toLowerCase() === 'true';

// Initialize FingerprintJS Pro with your API key and proxy endpoints
const fpPromise = FingerprintJS.load({
  apiKey: PUBLIC_KEY,
  endpoint: USE_CUSTOM_ENDPOINT ? (USE_PROXY ? `${FINGERPRINT_PROXY_BASE_URL}/visitors/identify` : FINGERPRINT_API_URL) : undefined,
  region: 'ap'  // Use 'ap' for Asia region
});

// Cache the visitor ID promise to prevent unnecessary API calls
let visitorIdPromise: Promise<string> | null = null;

const getVisitorId = async () => {
  if (!visitorIdPromise) {
    visitorIdPromise = fpPromise
      .then(fp => fp.get({ linkedId: 'session-' + Date.now() }))
      .then(result => result.visitorId);
  }
  return visitorIdPromise;
};

// Share the instance with ApiService using static method
ApiServiceWithPublicMethods.setFingerprintPromise(fpPromise);

// Styled console logging
const logStyles = {
  title: 'color: #1890ff; font-weight: bold; font-size: 14px;',
  success: 'color: #52c41a; font-weight: bold;',
  error: 'color: #f5222d; font-weight: bold;',
  info: 'color: #722ed1; font-weight: bold;',
  data: 'color: #fa8c16; font-weight: bold;'
};

const App: React.FC = () => {
  const [error, setError] = useState<string | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);
  const [loading, setLoading] = useState(true);
  const [visitorId, setVisitorId] = useState<string | null>(null);
  const [visitorData, setVisitorData] = useState<Record<string, any> | null>(null);
  const initializeRef = useRef(false);

  useEffect(() => {
    const initialize = async () => {
      // Skip if already initialized
      if (initializeRef.current) {
        return;
      }
      initializeRef.current = true;

      try {
        setIsInitializing(true);
        setError(null);

        // Initialize Fingerprint Pro
        console.log('%c🚀 Initializing Fingerprint Pro...', logStyles.title);
        console.log('%c📝 Configuration:', logStyles.info, {
          apiKey: PUBLIC_KEY ? 'Present' : 'Missing',
          proxyEnabled: USE_PROXY,
          proxyUrl: USE_PROXY ? FINGERPRINT_PROXY_BASE_URL : 'Not used',
          region: 'ap'
        });

        // Check if API key is present
        if (!PUBLIC_KEY) {
          throw new Error('Fingerprint API key is missing. Please check your environment variables.');
        }

        const fp = await fpPromise.catch(error => {
          // Handle script loading error
          if (error.message === FingerprintJS.ERROR_SCRIPT_LOAD_FAIL) {
            throw new Error('Unable to load Fingerprint service. This may be caused by an ad blocker or network issue.');
          }
          throw error;
        });
        
        console.log('%c✅ Fingerprint Pro SDK loaded successfully', logStyles.success);
        
        // Get visitor data
        console.log('%c🔍 Getting visitor data...', logStyles.title);
        const result = await fp.get({
          extendedResult: true
        });

        // Log full result in JSON format
        console.log('=== VISITOR DATA START ===');
        console.log(JSON.stringify(result, null, 2));
        console.log('=== VISITOR DATA END ===');

        setVisitorId(result.visitorId);
        setVisitorData(result);
        setLoading(false);
      } catch (error) {
        console.error('%c❌ Error:', logStyles.error, error);
        // Set user-friendly error message
        setError(error instanceof Error ? error.message : 'An unknown error occurred while initializing security features');
        // Stop loading state but keep error visible
        setLoading(false);
      } finally {
        setIsInitializing(false);
      }
    };

    initialize();
  }, []);

  if (error) {
    return (
      <Layout style={{ minHeight: '100vh', background: '#f0f2f5' }}>
        <Content style={{ padding: '24px' }}>
          <ErrorPage 
            error={error}
            suggestion={
              error.includes('ad blocker') 
                ? 'Try disabling your ad blocker or using a different browser.'
                : 'Please try refreshing the page or contact support if the issue persists.'
            }
          />
        </Content>
      </Layout>
    );
  }

  if (isInitializing) {
    return (
      <div style={{ 
        minHeight: '100vh', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%)'
      }}>
        <div style={{ textAlign: 'center' }}>
          <Spin 
            spinning={true}
            indicator={
              <LoadingOutlined 
                style={{ 
                  fontSize: 48,
                  color: '#1890ff'
                }} 
                spin 
              />
            }
          >
            <div style={{ marginTop: 24, padding: '0 16px' }}>
              <p>Initializing security features...</p>
            </div>
          </Spin>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <Layout style={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <div style={{ textAlign: 'center' }}>
          <Spin 
            spinning={true}
            size="large"
          >
            <div style={{ marginTop: 24, padding: '0 16px' }}>
              <p>Loading visitor information...</p>
            </div>
          </Spin>
        </div>
      </Layout>
    );
  }

  return (
    <Router>
      <Layout className="layout" style={{ minHeight: '100vh', background: '#f0f2f5' }}>
        <Content style={{ padding: '24px' }}>
          <Routes>
            <Route path="/" element={
              <Row gutter={24}>
                <Col span={6}>
                  <VisitorInfo 
                    fingerprint={visitorId} 
                    components={visitorData} 
                  />
                </Col>
                <Col span={18}>
          <StockTable />
                </Col>
              </Row>
            } />
            <Route path="/admin" element={<AdminDashboard />} />
          </Routes>
        </Content>
      </Layout>
    </Router>
  );
};

export default App; 