import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, token, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!token) {
    return null;
  }

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          🌾 Farmer to Consumer
        </Link>
        
        <div className="navbar-menu">
          {user?.role === 'FARMER' && (
            <>
              <Link to="/farmer" className="navbar-link">My Products</Link>
            </>
          )}
          
          {user?.role === 'CONSUMER' && (
            <>
              <Link to="/consumer/products" className="navbar-link">Browse Products</Link>
              <Link to="/consumer/cart" className="navbar-link">Cart</Link>
              <Link to="/consumer/orders" className="navbar-link">My Orders</Link>
            </>
          )}
          
          <div className="navbar-user">
            <span className="navbar-user-name">{user?.name || user?.email}</span>
            <span className="navbar-user-role">({user?.role})</span>
            <button onClick={handleLogout} className="btn btn-secondary btn-sm">
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

