 import React from 'react';
 import { useParams, useNavigate } from 'react-router-dom';
 import { useQuery } from '@tanstack/react-query';
 import { Descriptions, Button, Card, Spin, Tag, Space } from 'antd';
 import { ArrowLeftOutlined } from '@ant-design/icons';
 import dayjs from 'dayjs';
 import { passportApi } from '../api/client';

 const BatteryPassportDetail: React.FC = () => {
   const { id } = useParams<{ id: string }>();
   const navigate = useNavigate();

   const { data, isLoading } = useQuery({
     queryKey: ['passport', id],
     queryFn: () => passportApi.get(id!),
     enabled: !!id,
     select: (res) => res.data.data,
   });

   if (isLoading) return <Spin size="large" style={{ display: 'block', marginTop: 100 }} />;
   if (!data) return <div>护照不存在</div>;

   return (
     <div>
       <Space style={{ marginBottom: 16 }}>
         <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/passports')}>返回列表</Button>
       </Space>

       <Card title={`电池护照详情 - ${data.passportId}`}>
         <Descriptions column={2} bordered>
           <Descriptions.Item label="护照编号">{data.passportId}</Descriptions.Item>
           <Descriptions.Item label="序列号">{data.serialNumber}</Descriptions.Item>
           <Descriptions.Item label="产品型号">{data.productModel}</Descriptions.Item>
           <Descriptions.Item label="制造商">{data.manufacturer}</Descriptions.Item>
           <Descriptions.Item label="生产日期">
             {data.productionDate ? dayjs(data.productionDate).format('YYYY-MM-DD') : '-'}
           </Descriptions.Item>
           <Descriptions.Item label="电化学类型">{data.chemistryType || '-'}</Descriptions.Item>
           <Descriptions.Item label="额定容量(Ah)">{data.ratedCapacity ?? '-'}</Descriptions.Item>
           <Descriptions.Item label="额定电压(V)">{data.ratedVoltage ?? '-'}</Descriptions.Item>
           <Descriptions.Item label="初始SOH(%)">{data.initialSoh ?? '-'}</Descriptions.Item>
           <Descriptions.Item label="重量(kg)">{data.weight ?? '-'}</Descriptions.Item>
           <Descriptions.Item label="状态">
             <Tag color={data.status === 'ACTIVE' ? 'green' : 'orange'}>{data.status}</Tag>
           </Descriptions.Item>
           <Descriptions.Item label="创建时间">
             {data.createTime ? dayjs(data.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'}
           </Descriptions.Item>
         </Descriptions>
       </Card>

       {/* 此处为前端扩展点：天合/托邦可在此注入自定义组件 */}
       <div id="extension-passport-detail-bottom" style={{ marginTop: 16 }} />
     </div>
   );
 };

 export default BatteryPassportDetail;
