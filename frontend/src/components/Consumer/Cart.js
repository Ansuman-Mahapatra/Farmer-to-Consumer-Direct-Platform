import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../../context/CartContext';
import { consumerAPI } from '../../services/api';
import './Cart.css';

const Cart = () => {
  const { cart, updateQuantity, removeFromCart, clearCart, getTotalPrice } = useCart();
  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handlePlaceOrder = async () => {
    if (cart.length === 0) {
      setError('Your cart is empty');
      return;
    }

    if (!deliveryAddress.trim()) {
      setError('Please enter a delivery address');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const orderItems = cart.map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      }));

      const response = await consumerAPI.placeOrder({
        items: orderItems,
        deliveryAddress: deliveryAddress.trim(),
      });

      clearCart();
      navigate(`/consumer/orders/${response.data.order.id}`, {
        state: { order: response.data.order, mockOrderId: response.data.mockOrderId },
      });
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to place order');
    } finally {
      setLoading(false);
    }
  };

  if (cart.length === 0) {
    return (
      <div className="container">
        <div className="empty-state">
          <h3>Your cart is empty</h3>
          <p>Add some products to get started!</p>
          <button
            onClick={() => navigate('/consumer/products')}
            className="btn btn-primary"
          >
            Browse Products
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <h1>Shopping Cart</h1>

      <div className="cart-container">
        <div className="cart-items">
          {cart.map((item) => (
            <div key={item.product.id} className="cart-item">
              {item.product.imageUrl && (
                <img
                  src={item.product.imageUrl}
                  alt={item.product.name}
                  className="cart-item-image"
                />
              )}
              <div className="cart-item-details">
                <h3>{item.product.name}</h3>
                <p className="cart-item-category">{item.product.category}</p>
                <p className="cart-item-price">₹{item.product.pricePerKg}/kg</p>
              </div>
              <div className="cart-item-quantity">
                <label>Quantity (kg):</label>
                <div className="quantity-controls">
                  <button
                    onClick={() => updateQuantity(item.product.id, item.quantity - 0.5)}
                    className="quantity-btn"
                    disabled={item.quantity <= 0.5}
                  >
                    -
                  </button>
                  <input
                    type="number"
                    value={item.quantity}
                    onChange={(e) =>
                      updateQuantity(item.product.id, parseFloat(e.target.value) || 0)
                    }
                    min="0.5"
                    step="0.5"
                    className="quantity-input"
                  />
                  <button
                    onClick={() => updateQuantity(item.product.id, item.quantity + 0.5)}
                    className="quantity-btn"
                    disabled={item.quantity >= item.product.availableQuantity}
                  >
                    +
                  </button>
                </div>
                <p className="cart-item-total">
                  Total: ₹{(item.product.pricePerKg * item.quantity).toFixed(2)}
                </p>
              </div>
              <button
                onClick={() => removeFromCart(item.product.id)}
                className="btn btn-danger btn-sm"
              >
                Remove
              </button>
            </div>
          ))}
        </div>

        <div className="cart-summary">
          <div className="summary-card">
            <h2>Order Summary</h2>
            
            <div className="summary-row">
              <span>Subtotal:</span>
              <span>₹{getTotalPrice().toFixed(2)}</span>
            </div>
            
            <div className="summary-total">
              <span>Total:</span>
              <span>₹{getTotalPrice().toFixed(2)}</span>
            </div>

            <div className="form-group">
              <label>Delivery Address *</label>
              <textarea
                value={deliveryAddress}
                onChange={(e) => setDeliveryAddress(e.target.value)}
                placeholder="Enter your delivery address"
                rows="4"
                required
              />
            </div>

            {error && <div className="error-message">{error}</div>}

            <button
              onClick={handlePlaceOrder}
              className="btn btn-primary btn-large"
              disabled={loading || !deliveryAddress.trim()}
            >
              {loading ? 'Placing Order...' : 'Place Order'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;

