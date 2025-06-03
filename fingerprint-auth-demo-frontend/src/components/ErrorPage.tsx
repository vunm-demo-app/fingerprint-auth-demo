import React from 'react';
import { Result, Button, Typography } from 'antd';
import { ReloadOutlined, BugOutlined } from '@ant-design/icons';
import styled from 'styled-components';

const { Text, Paragraph } = Typography;

const ErrorContainer = styled.div`
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  padding: 20px;
`;

const StyledResult = styled(Result)`
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  max-width: 600px;
  width: 100%;

  .ant-result-icon {
    margin-bottom: 32px;
  }

  .ant-result-title {
    font-size: 28px;
    color: #1a1a1a;
  }

  .ant-result-subtitle {
    font-size: 16px;
    margin-top: 16px;
    color: #666;
  }
`;

const ErrorDetails = styled(Paragraph)`
  margin-top: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  font-family: monospace;
  font-size: 14px;
  color: #e63946;
  max-width: 100%;
  overflow-wrap: break-word;
`;

interface ErrorPageProps {
  error: string;
  suggestion?: string;
  onRetry?: () => void;
}

const ErrorPage: React.FC<ErrorPageProps> = ({ error, suggestion, onRetry }) => {
  const isInitializationError = error.includes('Failed to initialize');
  
  const getErrorTitle = () => {
    if (isInitializationError) {
      return 'Không thể khởi tạo ứng dụng';
    }
    return 'Đã xảy ra lỗi';
  };

  return (
    <ErrorContainer>
      <StyledResult
        icon={<BugOutlined style={{ fontSize: 72, color: '#e63946' }} />}
        title={getErrorTitle()}
        subTitle={
          <div>
            <p style={{ color: '#ff4d4f' }}>{error}</p>
            {suggestion && (
              <p style={{ color: '#8c8c8c', marginTop: '8px' }}>
                Suggestion: {suggestion}
              </p>
            )}
          </div>
        }
        extra={[
          <Button
            key="retry"
            type="primary"
            icon={<ReloadOutlined />}
            onClick={onRetry}
            size="large"
            style={{
              backgroundColor: '#2d6a4f',
              borderColor: '#2d6a4f',
              height: '44px',
              padding: '0 32px',
              fontSize: '16px',
            }}
          >
            Thử lại
          </Button>
        ]}
      >
        <ErrorDetails>
          <Text strong>Chi tiết lỗi:</Text>
          <br />
          {error}
        </ErrorDetails>
      </StyledResult>
    </ErrorContainer>
  );
};

export default ErrorPage; 