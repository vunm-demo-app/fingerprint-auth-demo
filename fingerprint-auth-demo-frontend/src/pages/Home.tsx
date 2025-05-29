import React from 'react';
import { Typography, Card } from 'antd';

const { Title, Paragraph } = Typography;

const Home: React.FC = () => {
  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '24px' }}>
      <Card>
        <Title level={2}>Fingerprint Authentication Demo</Title>
        <Paragraph>
          This is a demonstration of browser fingerprinting and advanced security measures.
          The application uses various techniques to identify and protect against potential threats:
        </Paragraph>
        <ul>
          <li>Browser fingerprinting for device identification</li>
          <li>Rate limiting and request validation</li>
          <li>IP-Fingerprint correlation analysis</li>
          <li>Timestamp validation and synchronization</li>
          <li>Bot detection and prevention</li>
        </ul>
        <Paragraph>
          The admin dashboard (accessible via /admin) provides detailed analytics and monitoring capabilities.
        </Paragraph>
      </Card>
    </div>
  );
};

export default Home; 