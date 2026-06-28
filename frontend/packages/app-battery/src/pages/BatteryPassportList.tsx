 import React, { useState } from 'react';
 import { Table, Button, Modal, Form, Input, InputNumber, DatePicker, Select, message, Space, Tag } from 'antd';
 import { PlusOutlined, SearchOutlined } from '@ant-design/icons';
 import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
 import dayjs from 'dayjs';
 import { useNavigate } from 'react-router-dom';
 import { passportApi } from '../api/client';

 const BatteryPassportList: React.FC = () => {
   const navigate = useNavigate();
   const queryClient = useQueryClient();
   const [page, setPage] = useState(1);
   const [searchSn, setSearchSn] = useState('');
   const [modalOpen, setModalOpen] = useState(false);
   const [form] = Form.useForm();

   const { data, isLoading } = useQuery({
     queryKey: ['passports', page, searchSn],
     queryFn: () => passportApi.list({ page, size: 20, ...(searchSn ? { serialNumber: searchSn } : {}) }),
     select: (res) => res.data.data,
   });

   const createMutation = useMutation({
     mutationFn: (values: any) => passportApi.create({
       ...values,
       productionDate: values.productionDate?.format('YYYY-MM-DD'),
     }),
     onSuccess: () => {
       message.success('创建成功');
       setModalOpen(false);
       form.resetFields();
       queryClient.invalidateQueries({ queryKey: ['passports'] });
     },
     onError: (err: any) => message.error('创建失败: ' + (err.response?.data?.message || err.message)),
   });

   const columns = [
     { title: '护照编号', dataIndex: 'passportId', key: 'passportId',
       render: (id: string) => <a onClick={() => navigate(`/passports/${id}`)}>{id}</a> },
     { title: '序列号', dataIndex: 'serialNumber', key: 'serialNumber' },
     { title: '产品型号', dataIndex: 'productModel', key: 'productModel' },
     { title: '制造商', dataIndex: 'manufacturer', key: 'manufacturer' },
     { title: '额定容量(Ah)', dataIndex: 'ratedCapacity', key: 'ratedCapacity' },
     { title: '状态', dataIndex: 'status', key: 'status',
       render: (s: string) => <Tag color={s === 'ACTIVE' ? 'green' : 'orange'}>{s}</Tag> },
     { title: '创建时间', dataIndex: 'createTime', key: 'createTime',
       render: (t: string) => dayjs(t).format('YYYY-MM-DD HH:mm') },
   ];

   return (
     <div>
       <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
         <Input.Search
           placeholder="搜索序列号"
           prefix={<SearchOutlined />}
           style={{ width: 300 }}
           onSearch={setSearchSn}
           allowClear
         />
         <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
           创建电池护照
         </Button>
       </div>

       <Table
         dataSource={data?.records}
         rowKey="id"
         loading={isLoading}
         columns={columns}
         pagination={{
           current: page,
           total: data?.total,
           pageSize: 20,
           onChange: setPage,
           showTotal: (total: number) => `共 ${total} 条`,
         }}
       />

       <Modal title="创建电池护照" open={modalOpen} onCancel={() => setModalOpen(false)}
              onOk={() => form.submit()} width={640}>
         <Form form={form} layout="vertical" onFinish={createMutation.mutate}>
           <Form.Item name="serialNumber" label="序列号" rules={[{ required: true }]}>
             <Input />
           </Form.Item>
           <Form.Item name="productModel" label="产品型号" rules={[{ required: true }]}>
             <Input />
           </Form.Item>
           <Form.Item name="manufacturer" label="制造商" rules={[{ required: true }]}>
             <Input />
           </Form.Item>
           <Form.Item name="chemistryType" label="电化学类型">
             <Select options={[
               { label: '磷酸铁锂(LFP)', value: 'LFP' },
               { label: '三元锂(NCM)', value: 'NCM' },
               { label: '钠离子', value: 'Sodium' },
             ]} />
           </Form.Item>
           <Form.Item name="ratedCapacity" label="额定容量(Ah)">
             <InputNumber style={{ width: '100%' }} min={0} />
           </Form.Item>
           <Form.Item name="productionDate" label="生产日期">
             <DatePicker style={{ width: '100%' }} />
           </Form.Item>
         </Form>
       </Modal>
     </div>
   );
 };

 export default BatteryPassportList;
