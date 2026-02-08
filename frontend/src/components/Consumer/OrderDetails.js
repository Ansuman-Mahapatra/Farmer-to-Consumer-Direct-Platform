import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { consumerAPI } from '../../services/api';
import './OrderDetails.css';

const OrderDetails = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [order, setOrder] = useState(location.state?.order || null);
  const [loading, setLoading] = useState(!order);
  const [error, setError] = useState('');
  const [paymentData, setPaymentData] = useState({
    razorpayPaymentId: '',
    razorpaySignature: '',
  });
  const [processingPayment, setProcessingPayment] = useState(false);
  const [paymentError, setPaymentError] = useState('');

  useEffect(() => {
    if (!order) {
      fetchOrder();
    }
  }, [orderId]);

  const fetchOrder = async () => {
    try {
      setLoading(true);
      const response = await consumerAPI.getOrder(orderId);
      setOrder(response.data);
      setError('');
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to fetch order details');
    } finally {
      setLoading(false);
    }
  };

  const handlePaymentConfirm = async (e) => {
    e.preventDefault();
    setProcessingPayment(true);
    setPaymentError('');

    try {
      // For mock payments, use any payment ID starting with 'mock_pay_'
      const paymentId = paymentData.razorpayPaymentId || `mock_pay_${Date.now()}`;
      const signature = paymentData.razorpaySignature || `mock_sig_${Date.now()}`;

      await consumerAPI.confirmPayment(orderId, {
        razorpayPaymentId: paymentId,
        razorpaySignature: signature,
      });

      // Refresh order to get updated status
      await fetchOrder();
      alert('Payment confirmed successfully!');
    } catch (err) {
      setPaymentError(err.response?.data?.error || 'Failed to confirm payment');
    } finally {
      setProcessingPayment(false);
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
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return <div className="loading">Loading order details...</div>;
  }

  if (error && !order) {
    return (
      <div className="container">
        <div className="error-message">{error}</div>
        <button onClick={() => navigate('/consumer/orders')} className="btn btn-secondary">
          Back to Orders
        </button>
      </div>
    );
  }

  if (!order) {
    return null;
  }

  return (
    <div className="container">
      <button onClick={() => navigate('/consumer/orders')} className="btn btn-secondary back-btn">
        ← Back to Orders
      </button>

      <div className="order-details">
        <div className="order-header-section">
          <div>
            <h1>Order #{order.id.substring(0, 8)}</h1>
            <p className="order-date">Placed on {formatDate(order.orderDate)}</p>
          </div>
          <div
            className="order-status-badge"
            style={{ backgroundColor: getStatusColor(order.status) }}
          >
            {order.status.replace(/_/g, ' ')}
          </div>
        </div>

        {order.status === 'PENDING_PAYMENT' && (
          <div className="payment-section card">
            <h2>Complete Payment</h2>
            <p className="payment-info">
              This is a mock payment system. Use any payment ID starting with 'mock_pay_' for testing.
            </p>
            <form onSubmit={handlePaymentConfirm}>
              <div className="form-group">
                <label>Payment ID (optional - will auto-generate if empty)</label>
                <input
                  type="text"
                  value={paymentData.razorpayPaymentId}
                  onChange={(e) =>
                    setPaymentData({
                      ...paymentData,
                      razorpayPaymentId: e.target.value,
                    })
                  }
                  placeholder="mock_pay_123456 (or leave empty)"
                />
              </div>
              <div className="form-group">
                <label>Payment Signature (optional - will auto-generate if empty)</label>
                <input
                  type="text"
                  value={paymentData.razorpaySignature}
                  onChange={(e) =>
                    setPaymentData({
                      ...paymentData,
                      razorpaySignature: e.target.value,
                    })
                  }
                  placeholder="mock_sig_123456 (or leave empty)"
                />
              </div>
              {paymentError && <div className="error-message">{paymentError}</div>}
              <button
                type="submit"
                className="btn btn-primary"
                disabled={processingPayment}
              >
                {processingPayment ? 'Processing...' : 'Confirm Payment'}
              </button>
            </form>
          </div>
        )}

        <div className="order-items-section card">
          <h2>Order Items</h2>
          <div className="order-items-list">
            {order.orderItems?.map((item, index) => (
              <div key={index} className="order-item">
                {item.product?.imageUrl && (
                  <img
                    src={item.product.imageUrl}
                    alt={item.product.name}
                    className="order-item-image"
                  />
                )}
                <div className="order-item-details">
                  <h3>{item.product?.name || 'Product'}</h3>
                  <p className="order-item-category">{item.product?.category}</p>
                  <p className="order-item-quantity">Quantity: {item.quantity} kg</p>
                  <p className="order-item-price">
                    ₹{item.product?.pricePerKg}/kg × {item.quantity} kg = ₹
                    {(item.product?.pricePerKg * item.quantity).toFixed(2)}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="order-info-section">
          <div className="card">
            <h2>Delivery Information</h2>
            <p><strong>Address:</strong> {order.deliveryAddress}</p>
            {order.deliveryPartner && (
              <p><strong>Delivery Partner:</strong> {order.deliveryPartner.name}</p>
            )}
          </div>

          <div className="card">
            <h2>Order Summary</h2>
            <div className="summary-row">
              <span>Subtotal:</span>
              <span>₹{order.totalAmount?.toFixed(2)}</span>
            </div>
            <div className="summary-total">
              <span>Total:</span>
              <span>₹{order.totalAmount?.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrderDetails;

