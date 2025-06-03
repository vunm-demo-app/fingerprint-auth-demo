import React from 'react';
import { Card, Typography, Descriptions, Space } from 'antd';
import { 
    SafetyOutlined, 
    IdcardOutlined,
    ChromeOutlined,
    EyeOutlined,
    HistoryOutlined,
    InfoCircleOutlined
} from '@ant-design/icons';
import styled from 'styled-components';

const { Text } = Typography;

const StyledCard = styled(Card)`
    .ant-card-head {
        background: #f5f5f5;
        border-bottom: 1px solid #e8e8e8;
    }
    .ant-card-head-title {
        font-size: 16px;
        font-weight: 500;
    }
`;

const ValueText = styled(Text)`
    background: #f0f5ff;
    padding: 4px 8px;
    border-radius: 4px;
    border: 1px solid #d6e4ff;
    font-family: monospace;
    font-size: 14px;
    color: #2f54eb;
`;

interface VisitorInfoProps {
    fingerprint: string | null;
    components: any;
}

const VisitorInfo: React.FC<VisitorInfoProps> = ({ fingerprint, components }) => {
    if (!fingerprint || !components) {
        return null;
    }

    const formatDate = (dateStr: string) => {
        return new Date(dateStr).toLocaleString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        });
    };

    // Log để debug
    console.log('VisitorInfo components:', components);

    return (
        <StyledCard 
            title={
                <Space>
                    <SafetyOutlined style={{ color: '#1890ff' }} />
                    <span>Thông tin thiết bị</span>
                </Space>
            }
            style={{ marginBottom: 16 }}
        >
            <Descriptions column={1} size="small">
                <Descriptions.Item label={
                    <Space>
                        <IdcardOutlined />
                        <span>Mã định danh</span>
                    </Space>
                }>
                    <ValueText copyable>{components?.visitorId || 'N/A'}</ValueText>
                </Descriptions.Item>

                <Descriptions.Item label={
                    <Space>
                        <InfoCircleOutlined />
                        <span>Thông tin cơ bản</span>
                    </Space>
                }>
                    <Space direction="vertical" size="small">
                        <Text>IP: {components?.ipInfo?.ip || 'N/A'}</Text>
                        <Text>Trình duyệt: {components?.browserDetails?.name || 'N/A'} {components?.browserDetails?.version || ''}</Text>
                        <Text>Hệ điều hành: {components?.osDetails?.name || 'N/A'} {components?.osDetails?.version || ''}</Text>
                        <Text>Thiết bị: {components?.deviceDetails?.type || 'N/A'}</Text>
                        <Text>Chế độ ẩn danh: {components?.incognito ? 'Có' : 'Không'}</Text>
                    </Space>
                </Descriptions.Item>

                <Descriptions.Item label={
                    <Space>
                        <HistoryOutlined />
                        <span>Lịch sử truy cập</span>
                    </Space>
                }>
                    <Space direction="vertical" size="small">
                        <Text>Lần đầu truy cập: {components?.firstSeenAt?.global ? formatDate(components.firstSeenAt.global) : 'Không có dữ liệu'}</Text>
                        <Text>Lần cuối truy cập: {components?.lastSeenAt?.global ? formatDate(components.lastSeenAt.global) : 'Không có dữ liệu'}</Text>
                    </Space>
                </Descriptions.Item>

                <Descriptions.Item label={
                    <Space>
                        <EyeOutlined />
                        <span>Độ tin cậy</span>
                    </Space>
                }>
                    <Space direction="vertical" size="small">
                        <Text>Phiên bản: {components?.meta?.version || 'N/A'}</Text>
                        <Text>Điểm tin cậy: {components?.confidence?.score || 'N/A'}</Text>
                        <Text>Phiên bản đánh giá: {components?.confidence?.revision || 'N/A'}</Text>
                    </Space>
                </Descriptions.Item>

                <Descriptions.Item label={
                    <Space>
                        <ChromeOutlined />
                        <span>Thông tin yêu cầu</span>
                    </Space>
                }>
                    <Space direction="vertical" size="small">
                        <Text>Request ID: {components?.requestId || 'N/A'}</Text>
                        <Text>Trạng thái: {components?.visitorFound ? 'Đã tìm thấy' : 'Chưa tìm thấy'}</Text>
                    </Space>
                </Descriptions.Item>

                <Descriptions.Item label={
                    <Space>
                        <InfoCircleOutlined />
                        <span>Lưu ý</span>
                    </Space>
                }>
                    <Text type="secondary">
                        Để xem thêm thông tin chi tiết về thiết bị (IP, trình duyệt, hệ điều hành), 
                        vui lòng nâng cấp lên FingerprintJS Pro.
                    </Text>
                </Descriptions.Item>
            </Descriptions>
        </StyledCard>
    );
};

export default VisitorInfo;
