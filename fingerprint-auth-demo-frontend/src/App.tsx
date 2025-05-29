import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { Layout, Menu } from 'antd';
import StockTable from './components/StockTable';
import AdminDashboard from './pages/AdminDashboard';

const { Header, Content } = Layout;

const App: React.FC = () => {
  return (
    <Router>
      <Layout className="layout" style={{ minHeight: '100vh' }}>
        <Header>
          <Menu theme="dark" mode="horizontal" defaultSelectedKeys={['1']}>
            <Menu.Item key="1">
              <Link to="/">Trading</Link>
            </Menu.Item>
            <Menu.Item key="2">
              <Link to="/admin">Admin</Link>
            </Menu.Item>
          </Menu>
        </Header>
        <Content style={{ padding: '0 50px', marginTop: '16px' }}>
          <Routes>
            <Route path="/" element={<StockTable />} />
            <Route path="/admin" element={<AdminDashboard />} />
          </Routes>
        </Content>
      </Layout>
    </Router>
  );
};

export default App; 