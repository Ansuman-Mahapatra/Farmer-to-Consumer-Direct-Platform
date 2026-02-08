import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import FarmerDashboard from './components/Farmer/FarmerDashboard';
import ConsumerDashboard from './components/Consumer/ConsumerDashboard';
import ProductList from './components/Consumer/ProductList';
import Cart from './components/Consumer/Cart';
import Orders from './components/Consumer/Orders';
import OrderDetails from './components/Consumer/OrderDetails';
import Navbar from './components/Shared/Navbar';
import './App.css';

function PrivateRoute({ children, allowedRoles }) {
  const { user, token } = useAuth();
  
  if (!token) {
    return <Navigate to="/login" />;
  }
  
  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" />;
  }
  
  return children;
}

function AppRoutes() {
  const { user, token } = useAuth();
  
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/login" element={!token ? <Login /> : <Navigate to={user?.role === 'FARMER' ? '/farmer' : '/consumer'} />} />
        <Route path="/register" element={!token ? <Register /> : <Navigate to={user?.role === 'FARMER' ? '/farmer' : '/consumer'} />} />
        
        <Route 
          path="/farmer" 
          element={
            <PrivateRoute allowedRoles={['FARMER', 'ADMIN']}>
              <FarmerDashboard />
            </PrivateRoute>
          } 
        />
        
        <Route 
          path="/consumer" 
          element={
            <PrivateRoute allowedRoles={['CONSUMER', 'ADMIN']}>
              <ConsumerDashboard />
            </PrivateRoute>
          } 
        />
        
        <Route 
          path="/consumer/products" 
          element={
            <PrivateRoute allowedRoles={['CONSUMER', 'ADMIN']}>
              <ProductList />
            </PrivateRoute>
          } 
        />
        
        <Route 
          path="/consumer/cart" 
          element={
            <PrivateRoute allowedRoles={['CONSUMER', 'ADMIN']}>
              <Cart />
            </PrivateRoute>
          } 
        />
        
        <Route 
          path="/consumer/orders" 
          element={
            <PrivateRoute allowedRoles={['CONSUMER', 'ADMIN']}>
              <Orders />
            </PrivateRoute>
          } 
        />
        
        <Route 
          path="/consumer/orders/:orderId" 
          element={
            <PrivateRoute allowedRoles={['CONSUMER', 'ADMIN']}>
              <OrderDetails />
            </PrivateRoute>
          } 
        />
        
        <Route path="/" element={<Navigate to={token ? (user?.role === 'FARMER' ? '/farmer' : '/consumer') : '/login'} />} />
      </Routes>
    </>
  );
}

function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <Router>
          <AppRoutes />
        </Router>
      </CartProvider>
    </AuthProvider>
  );
}

export default App;

