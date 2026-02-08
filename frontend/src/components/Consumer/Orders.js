import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { consumerAPI } from '../../services/api';
import './Orders.css';

const Orders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const response = await consumerAPI.getMyOrders();
      setOrders(response.data);
      setError('');
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to fetch orders');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    const statusColors = {
      PENDING_PAYMENT: '#ff9800',
      CONFIRMED: '#2196F3',
      PREPARING: '#9c27b0',
      READY_FOR_PICKUP: '#00bcd4',
      PICKED_UP: '#3f51b5',
      IN_TRANSIT: '#009688',
      DELIVERED: '#4CAF50',
      CANCELLED: '#f44336',
    };
    return statusColors[status] || '#666';
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return <div className="loading">Loading your orders...</div>;
  }

  if (orders.length === 0) {
    return (
      <div className="container">
        <div className="empty-state">
          <h3>No orders yet</h3>
          <p>Start shopping to see your orders here!</p>
          <Link to="/consumer/products" className="btn btn-primary">
            Browse Products
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <h1>My Orders</h1>
      {error && <div className="error-message">{error}</div>}

      <div className="orders-list">
        {orders.map((order) => (
          <Link
            key={order.id}
            to={`/consumer/orders/${order.id}`}
            className="order-card"
          >
            <div className="order-header">
              <div>
                <h3>Order #{order.id.substring(0, 8)}</h3>
                <p className="order-date">{formatDate(order.orderDate)}</p>
              </div>
              <div
                className="order-status"
                style={{ backgroundColor: getStatusColor(order.status) }}
              >
                {order.status.replace(/_/g, ' ')}
              </div>
            </div>

            <div className="order-items-preview">
              {order.orderItems?.slice(0, 3).map((item, index) => (
                <span key={index} className="order-item-tag">
                  {item.product?.name || 'Product'} ({item.quantity} kg)
                </span>
              ))}
              {order.orderItems?.length > 3 && (
                <span className="order-item-tag">
                  +{order.orderItems.length - 3} more
                </span>
              )}
            </div>

            <div className="order-footer">
              <div className="order-address">
                <strong>Delivery to:</strong> {order.deliveryAddress}
              </div>
              <div className="order-total">
                Total: <strong>₹{order.totalAmount?.toFixed(2)}</strong>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default Orders;

