import React, { useEffect, useState } from 'react';
import { Table, Typography, Spin } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { apiService, type StockPrice } from '../services/api';

const StockTable: React.FC = () => {
    const [data, setData] = useState<StockPrice[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

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
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);
                const stocks = await apiService.getAllStocks();
                setData(stocks);
            } catch (err) {
                setError(err instanceof Error ? err.message : 'Failed to fetch data');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
        const interval = setInterval(fetchData, 5000); // Refresh every 5 seconds

        return () => clearInterval(interval);
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