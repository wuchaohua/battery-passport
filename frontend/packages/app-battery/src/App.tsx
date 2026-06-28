 import React from 'react';
 import { ConfigProvider, Layout, Menu, theme } from 'antd';
 import { Routes, Route, useNavigate, useLocation } from 'react-router-dom';
 import { SafetyCertificateOutlined, FileTextOutlined, TeamOutlined } from '@ant-design/icons';
 import zhCN from 'antd/locale/zh_CN';
 import BatteryPassportList from './pages/BatteryPassportList';
 import BatteryPassportDetail from './pages/BatteryPassportDetail';

 const { Header, Sider, Content } = Layout;

 const menuItems = [
   { key: '/passports', icon: <SafetyCertificateOutlined />, label: '电池护照' },
   { key: '/reports', icon: <FileTextOutlined />, label: '报告管理' },
   { key: '/tenants', icon: <TeamOutlined />, label: '租户管理' },
 ];

 // 动态品牌色（根据构建模式注入）
 const brandColors: Record<string, string> = {
   trina: '#003366',
   tuobang: '#c9362b',
   default: '#1677ff',
 };

 const App: React.FC = () => {
   const navigate = useNavigate();
   const location = useLocation();
   const deployMode = (window as any).__DEPLOY_MODE__ || 'onpremise';
   const brand = (window as any).__BRAND__ || 'default';
   const primaryColor = brandColors[brand] || brandColors.default;

   return (
     <ConfigProvider
       locale={zhCN}
       theme={{
         token: { colorPrimary: primaryColor, borderRadius: 6 },
       }}
     >
       <Layout style={{ minHeight: '100vh' }}>
         <Sider trigger={null}>
           <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontWeight: 'bold', fontSize: 18 }}>
             电池护照 {deployMode === 'saas' ? '(SaaS)' : ''}
           </div>
           <Menu
             theme="dark"
             mode="inline"
             selectedKeys={[location.pathname]}
             items={menuItems.filter(item => {
               // SaaS模式显示租户管理，私有化隐藏
               if (deployMode !== 'saas' && item.key === '/tenants') return false;
               return true;
             })}
             onClick={({ key }) => navigate(key)}
           />
         </Sider>
         <Layout>
           <Content style={{ margin: 24, padding: 24, background: '#fff', borderRadius: 8 }}>
             <Routes>
               <Route path="/" element={<BatteryPassportList />} />
               <Route path="/passports" element={<BatteryPassportList />} />
               <Route path="/passports/:id" element={<BatteryPassportDetail />} />
             </Routes>
           </Content>
         </Layout>
       </Layout>
     </ConfigProvider>
   );
 };

 export default App;
