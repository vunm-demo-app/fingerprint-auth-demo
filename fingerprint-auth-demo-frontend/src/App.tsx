import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Layout } from 'antd';
import Home from './pages/Home';
import AdminDashboard from './pages/AdminDashboard';

const { Content } = Layout;

const App: React.FC = () => {
  return (
    <Router>
      <Layout className="layout" style={{ minHeight: '100vh' }}>
        <Content style={{ padding: '0 50px', marginTop: '16px' }}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/admin" element={<AdminDashboard />} />
          </Routes>
        </Content>
      </Layout>
    </Router>
  );
};

export default App; 