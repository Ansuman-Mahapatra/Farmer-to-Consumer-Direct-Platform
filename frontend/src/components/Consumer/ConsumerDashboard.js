import React from 'react';
import { Link } from 'react-router-dom';
import './ConsumerDashboard.css';

const ConsumerDashboard = () => {
  return (
    <div className="container">
      <div className="consumer-dashboard">
        <h1>Welcome to Farmer to Consumer Platform</h1>
        <p className="dashboard-subtitle">Buy fresh produce directly from farmers</p>
        
        <div className="dashboard-actions">
          <Link to="/consumer/products" className="action-card">
            <div className="action-icon">🛒</div>
            <h3>Browse Products</h3>
            <p>Explore fresh produce from local farmers</p>
          </Link>
          
          <Link to="/consumer/cart" className="action-card">
            <div className="action-icon">🛍️</div>
            <h3>My Cart</h3>
            <p>Review and checkout your items</p>
          </Link>
          
          <Link to="/consumer/orders" className="action-card">
            <div className="action-icon">📦</div>
            <h3>My Orders</h3>
            <p>Track your orders and deliveries</p>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ConsumerDashboard;

