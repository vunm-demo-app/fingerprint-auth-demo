import React, { useEffect, useState, useRef } from 'react';
import { Table, Typography, Spin } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { apiService, type StockPrice } from '../services/api';

const StockTable: React.FC = () => {
    const [data, setData] = useState<StockPrice[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    // Use ref instead of state since we only need to track the count internally
    const consecutive401CountRef = useRef(0);
    const intervalRef = useRef<NodeJS.Timeout | null>(null);

    const getPriceColor = (price: number, refPrice: number) => {
        if (price > refPrice) return '#00B14F';
        if (price < refPrice) return '#FF0000';
        return '#FFA500';
    };

    const columns: ColumnsType<StockPrice> = [
        {
            title: 'Mã CK',
            dataIndex: 'symbol',
            key: 'symbol',
            fixed: 'left',
            width: 100,
            render: (text) => (
                <Typography.Text strong style={{ fontSize: '14px' }}>
                    {text}
                </Typography.Text>
            ),
        },
        {
            title: 'Tham chiếu',
            dataIndex: 'refPrice',
            key: 'refPrice',
            align: 'right',
            width: 100,
            render: (price) => (
                <Typography.Text style={{ color: '#FFA500', fontSize: '14px' }}>
                    {price.toFixed(2)}
                </Typography.Text>
            ),
        },
        {
            title: 'Trần',
            dataIndex: 'ceilingPrice',
            key: 'ceilingPrice',
            align: 'right',
            width: 100,
            render: (price) => (
                <Typography.Text style={{ color: '#FF0000', fontSize: '14px' }}>
                    {price.toFixed(2)}
                </Typography.Text>
            ),
        },
        {
            title: 'Sàn',
            dataIndex: 'floorPrice',
            key: 'floorPrice',
            align: 'right',
            width: 100,
            render: (price) => (
                <Typography.Text style={{ color: '#00B14F', fontSize: '14px' }}>
                    {price.toFixed(2)}
                </Typography.Text>
            ),
        },
        {
            title: 'Khớp lệnh',
            dataIndex: 'matchPrice',
            key: 'matchPrice',
            align: 'right',
            width: 100,
            render: (price, record) => (
                <Typography.Text style={{ 
                    color: getPriceColor(price, record.refPrice),
                    fontSize: '14px',
                    fontWeight: 'bold'
                }}>
                    {price.toFixed(2)}
                </Typography.Text>
            ),
        },
        {
            title: 'Thay đổi',
            key: 'change',
            align: 'right',
            width: 150,
            render: (_, record) => (
                <Typography.Text style={{ 
                    color: getPriceColor(record.matchPrice, record.refPrice),
                    fontSize: '14px'
                }}>
                    {record.change > 0 ? '+' : ''}{record.change.toFixed(2)} ({record.changePercent.toFixed(2)}%)
                </Typography.Text>
            ),
        },
        {
            title: 'KL',
            dataIndex: 'volume',
            key: 'volume',
            align: 'right',
            width: 120,
            render: (volume) => (
                <Typography.Text style={{ fontSize: '14px' }}>
                    {volume.toLocaleString()}
                </Typography.Text>
            ),
        },
    ];

    useEffect(() => {
        let isUnmounted = false;
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);
                const stocks = await apiService.getAllStocks();
                if (!isUnmounted) {
                    setData(stocks);
                    consecutive401CountRef.current = 0; // Reset on success
                }
            } catch (err: any) {
                if (err?.response?.status === 401) {
                    consecutive401CountRef.current += 1;
                    if (consecutive401CountRef.current >= 5 && intervalRef.current) {
                        clearInterval(intervalRef.current);
                        intervalRef.current = null;
                    }
                    setError('Unauthorized (401): Đã dừng tự động làm mới sau 5 lần lỗi.');
                } else {
                    setError(err instanceof Error ? err.message : 'Failed to fetch data');
                }
            } finally {
                setLoading(false);
            }
        };

        fetchData();
        intervalRef.current = setInterval(fetchData, 5000); // Refresh every 5 seconds

        return () => {
            isUnmounted = true;
            if (intervalRef.current) {
                clearInterval(intervalRef.current);
            }
        };
    }, []);

    if (error) {
        return (
            <Typography.Text type="danger" style={{ padding: '20px', display: 'block' }}>
                {error}
            </Typography.Text>
        );
    }

    return (
        <Spin spinning={loading}>
            <Table
                columns={columns}
                dataSource={data}
                rowKey="symbol"
                pagination={false}
                scroll={{ x: 'max-content', y: 'calc(100vh - 200px)' }}
                size="small"
                bordered
                style={{
                    backgroundColor: '#FFFFFF',
                    borderRadius: 8,
                    overflow: 'hidden',
                }}
            />
        </Spin>
    );
};

export default StockTable; 