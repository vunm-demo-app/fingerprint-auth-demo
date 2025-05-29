import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout } from 'antd';
import StockTable from './components/StockTable';
import AdminDashboard from './pages/AdminDashboard';

const { Content } = Layout;

const App: React.FC = () => {
  return (
    <Router>
      <Layout className="layout" style={{ minHeight: '100vh', background: '#f0f2f5' }}>
        <Content style={{ padding: '24px' }}>
          <Routes>
            <Route path="/" element={<StockTable />} />
            {/* Admin route is only accessible via direct URL */}
            <Route path="/admin" element={<AdminDashboard />} />
          </Routes>
        </Content>
      </Layout>
    </Router>
  );
};

export default App; 