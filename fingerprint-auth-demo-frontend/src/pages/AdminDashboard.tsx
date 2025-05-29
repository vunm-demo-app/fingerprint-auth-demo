import React, { useState, useEffect } from 'react';
import { Card, Table, DatePicker, Space, Row, Col, Statistic, Alert } from 'antd';
import { Line } from '@ant-design/charts';
import { apiService } from '../services/api';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

interface RequestLog {
    id: string;
    fingerprint: string;
    deviceId: string;
    ipAddress: string;
    userAgent: string;
    requestType: string;
    isSuccess: boolean;
    failureReason?: string;
    timestamp: string;
    isSuspectedBot: boolean;
}

interface Statistics {
    totalRequests: number;
    uniqueIps: number;
    uniqueFingerprints: number;
    botAttempts: number;
    failedRequests: number;
}

const AdminDashboard: React.FC = () => {
    const [dateRange, setDateRange] = useState<[dayjs.Dayjs, dayjs.Dayjs]>([
        dayjs().subtract(7, 'day'),
        dayjs()
    ]);
    const [stats, setStats] = useState<Statistics | null>(null);
    const [logs, setLogs] = useState<RequestLog[]>([]);
    const [correlation, setCorrelation] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchData = async () => {
        try {
            setLoading(true);
            setError(null);

            // Fetch statistics
            const statsResponse = await apiService.getAdminStats(
                dateRange[0].toISOString(),
                dateRange[1].toISOString()
            );
            setStats(statsResponse.data);

            // Fetch logs
            const logsResponse = await apiService.getAdminLogs({
                from: dateRange[0].toISOString(),
                to: dateRange[1].toISOString(),
                page: 0,
                size: 100
            });
            setLogs(logsResponse.data.content);

            // Fetch correlation data
            const correlationResponse = await apiService.getIpFingerprintCorrelation();
            setCorrelation(correlationResponse.data);
        } catch (err) {
            setError('Failed to fetch data. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [dateRange]);

    const columns = [
        {
            title: 'Timestamp',
            dataIndex: 'timestamp',
            key: 'timestamp',
            render: (text: string) => dayjs(text).format('YYYY-MM-DD HH:mm:ss')
        },
        {
            title: 'Fingerprint',
            dataIndex: 'fingerprint',
            key: 'fingerprint',
        },
        {
            title: 'IP Address',
            dataIndex: 'ipAddress',
            key: 'ipAddress',
        },
        {
            title: 'Request Type',
            dataIndex: 'requestType',
            key: 'requestType',
        },
        {
            title: 'Bot Suspected',
            dataIndex: 'isSuspectedBot',
            key: 'isSuspectedBot',
            render: (suspected: boolean) => (
                <span style={{ color: suspected ? '#f5222d' : '#52c41a' }}>
                    {suspected ? 'Yes' : 'No'}
                </span>
            )
        }
    ];

    const correlationConfig = {
        data: correlation,
        xField: 'fingerprint',
        yField: 'ipCount',
        point: {
            size: 5,
            shape: 'diamond',
        },
        label: {
            style: {
                fill: '#aaa',
            },
        },
    };

    return (
        <div style={{ padding: '24px' }}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
                {error && (
                    <Alert
                        message="Error"
                        description={error}
                        type="error"
                        showIcon
                    />
                )}

                <Card title="Date Range">
                    <RangePicker
                        value={dateRange}
                        onChange={(dates) => {
                            if (dates && dates[0] && dates[1]) {
                                setDateRange([dates[0], dates[1]]);
                            }
                        }}
                        showTime
                    />
                </Card>

                {stats && (
                    <Row gutter={16}>
                        <Col span={4}>
                            <Card>
                                <Statistic
                                    title="Total Requests"
                                    value={stats.totalRequests}
                                />
                            </Card>
                        </Col>
                        <Col span={4}>
                            <Card>
                                <Statistic
                                    title="Unique IPs"
                                    value={stats.uniqueIps}
                                />
                            </Card>
                        </Col>
                        <Col span={4}>
                            <Card>
                                <Statistic
                                    title="Unique Fingerprints"
                                    value={stats.uniqueFingerprints}
                                />
                            </Card>
                        </Col>
                        <Col span={4}>
                            <Card>
                                <Statistic
                                    title="Bot Attempts"
                                    value={stats.botAttempts}
                                    valueStyle={{ color: '#cf1322' }}
                                />
                            </Card>
                        </Col>
                        <Col span={4}>
                            <Card>
                                <Statistic
                                    title="Failed Requests"
                                    value={stats.failedRequests}
                                    valueStyle={{ color: '#faad14' }}
                                />
                            </Card>
                        </Col>
                    </Row>
                )}

                <Card title="IP-Fingerprint Correlation">
                    <Line {...correlationConfig} />
                </Card>

                <Card title="Request Logs">
                    <Table
                        dataSource={logs}
                        columns={columns}
                        rowKey="id"
                        loading={loading}
                        pagination={{ pageSize: 10 }}
                    />
                </Card>
            </Space>
        </div>
    );
};

export default AdminDashboard; 