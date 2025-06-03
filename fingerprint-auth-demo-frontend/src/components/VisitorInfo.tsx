import React from 'react';
import { Card, Typography, Descriptions, Tag, Space } from 'antd';
import { 
    SafetyOutlined, 
    GlobalOutlined, 
    ClockCircleOutlined, 
    IdcardOutlined,
    DesktopOutlined,
    ChromeOutlined,
    EyeOutlined,
    HistoryOutlined,
    InfoCircleOutlined
} from '@ant-design/icons';
import styled from 'styled-components';

const { Title, Text } = Typography;

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
    components: {
        visitorId: string;
        requestId: string;
        browserName: string;
        browserVersion: string;
        confidence: {
            revision: string;
            score: number;
        };
        device: string;
        firstSeenAt: {
            global: string;
            subscription: string;
        };
        incognito: boolean;
        ip: string;
        lastSeenAt: {
            global: string;
            subscription: string;
        };
        meta: {
            version: string;
        };
        os: string;
        osVersion: string;
        visitorFound: boolean;
    } | null;
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
                    <ValueText copyable>{components.visitorId}</ValueText>
                </Descriptions.Item>

                <Descriptions.Item label={
                    <Space>
                        <InfoCircleOutlined />
                        <span>Thông tin cơ bản</span>
                    </Space>
                }>
                    <Space direction="vertical" size="small">
                        <Text>IP: {components.ip}</Text>
                        <Text>Trình duyệt: {components.browserName} {components.browserVersion}</Text>
                        <Text>Hệ điều hành: {components.os} {components.osVersion}</Text>
                        <Text>Thiết bị: {components.device}</Text>
                        <Text>Chế độ ẩn danh: {components.incognito ? 'Có' : 'Không'}</Text>
                    </Space>
                </Descriptions.Item>

                <Descriptions.Item label={
                    <Space>
                        <HistoryOutlined />
                        <span>Lịch sử truy cập</span>
                    </Space>
                }>
                    <Space direction="vertical" size="small">
                        <Text>Lần đầu truy cập: {formatDate(components.firstSeenAt.global)}</Text>
                        <Text>Lần cuối truy cập: {formatDate(components.lastSeenAt.global)}</Text>
                    </Space>
                </Descriptions.Item>

                <Descriptions.Item label={
                    <Space>
                        <EyeOutlined />
                        <span>Độ tin cậy</span>
                    </Space>
                }>
                    <Space direction="vertical" size="small">
                        <Text>Phiên bản: {components.meta.version}</Text>
                        <Text>Điểm tin cậy: {components.confidence.score}</Text>
                        <Text>Phiên bản đánh giá: {components.confidence.revision}</Text>
                    </Space>
                </Descriptions.Item>

                <Descriptions.Item label={
                    <Space>
                        <ChromeOutlined />
                        <span>Thông tin yêu cầu</span>
                    </Space>
                }>
                    <Space direction="vertical" size="small">
                        <Text>Request ID: {components.requestId}</Text>
                        <Text>Trạng thái: {components.visitorFound ? 'Đã tìm thấy' : 'Chưa tìm thấy'}</Text>
                    </Space>
                </Descriptions.Item>
            </Descriptions>
        </StyledCard>
    );
};

export default VisitorInfo;
