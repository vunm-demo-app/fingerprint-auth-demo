import React, { useEffect, useState, useRef } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout, Spin, Row, Col } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';
import StockTable from './components/StockTable';
import AdminDashboard from './pages/AdminDashboard';
import ErrorPage from './components/ErrorPage';
import { ApiServiceWithPublicMethods } from './services/api';
import FingerprintJS from '@fingerprintjs/fingerprintjs-pro';
import VisitorInfo from './components/VisitorInfo';

const { Content } = Layout;

const FINGERPRINT_PROXY_BASE_URL = import.meta.env.VITE_FINGERPRINT_PROXY_BASE_URL || 'http://localhost:8080';
const USE_PROXY = (import.meta.env.VITE_FINGERPRINT_USE_PROXY || 'false').toLowerCase() === 'true';
const PUBLIC_KEY = import.meta.env.VITE_FINGERPRINT_PUBLIC_KEY || '';
const FINGERPRINT_API_URL = import.meta.env.VITE_FINGERPRINT_API_URL || 'https://api.fpjs.io';

// Initialize FingerprintJS Pro with your API key and proxy endpoints
console.log('FingerprintJS Config:', {
  apiKey: PUBLIC_KEY ? 'API Key is set' : 'API Key is missing',
  endpoint: USE_PROXY ? `${FINGERPRINT_PROXY_BASE_URL}/api/fpjs` : FINGERPRINT_API_URL,
  region: 'ap'
});

const fpPromise = FingerprintJS.load({
  apiKey: PUBLIC_KEY,
  endpoint: USE_PROXY ? `${FINGERPRINT_PROXY_BASE_URL}/api/fpjs` : FINGERPRINT_API_URL,
  region: 'ap',  // Use 'ap' for Asia region
  // Enable advanced device information
  extendedResult: true,
  // Enable additional signals
  signals: [
    'browserDetails',
    'osDetails',
    'deviceDetails',
    'ipInfo',
    'incognito'
  ]
} as any); // Type assertion to bypass TypeScript check

// Share the instance with ApiService using static method
ApiServiceWithPublicMethods.setFingerprintPromise(fpPromise);

const App: React.FC = () => {
  const [error, setError] = useState<string | null>(null);
  const [isInitializing, setIsInitializing] = useState(true);
  const [loading, setLoading] = useState(true);
  const [visitorId, setVisitorId] = useState<string | null>(null);
  const [visitorData, setVisitorData] = useState<any>(null);
  const initializeRef = useRef(false);

  useEffect(() => {
    const initialize = async () => {
      if (initializeRef.current) return;
      initializeRef.current = true;

      try {
        setIsInitializing(true);
        setError(null);

        const fp = await fpPromise;
        const result = await fp.get();
        console.log('=== VISITOR DATA START ===');
        console.log(JSON.stringify(result, null, 2));
        console.log('=== VISITOR DATA END ===');

        setVisitorId(result.visitorId);
        setVisitorData(result);
        setLoading(false);
      } catch (err) {
        console.error('Error initializing FingerprintJS:', err);
        setError(err instanceof Error ? err.message : 'Failed to initialize FingerprintJS');
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