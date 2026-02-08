import React, { useState, useEffect } from 'react';
import { farmerAPI } from '../../services/api';
import ProductList from './ProductList';
import AddProductForm from './AddProductForm';
import EditProductForm from './EditProductForm';
import './FarmerDashboard.css';

const FarmerDashboard = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAddForm, setShowAddForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await farmerAPI.getMyProducts();
      setProducts(response.data);
      setError('');
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to fetch products');
    } finally {
      setLoading(false);
    }
  };

  const handleAddProduct = async (productData) => {
    try {
      await farmerAPI.addProduct(productData);
      setShowAddForm(false);
      fetchProducts();
    } catch (err) {
      throw new Error(err.response?.data?.error || 'Failed to add product');
    }
  };

  const handleUpdateProduct = async (productId, productData) => {
    try {
      await farmerAPI.updateProduct(productId, productData.productData, productData.imageFile);
      setEditingProduct(null);
      fetchProducts();
    } catch (err) {
      throw new Error(err.response?.data?.error || 'Failed to update product');
    }
  };

  const handleUpdateInventory = async (productId, quantity) => {
    try {
      await farmerAPI.updateInventory(productId, quantity);
      fetchProducts();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to update inventory');
    }
  };

  if (loading) {
    return <div className="loading">Loading your products...</div>;
  }

  return (
    <div className="container">
      <div className="dashboard-header">
        <h1>Farmer Dashboard</h1>
        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="btn btn-primary"
        >
          {showAddForm ? 'Cancel' : '+ Add New Product'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {showAddForm && (
        <AddProductForm
          onSubmit={handleAddProduct}
          onCancel={() => setShowAddForm(false)}
        />
      )}

      {editingProduct && (
        <EditProductForm
          product={editingProduct}
          onSubmit={handleUpdateProduct}
          onCancel={() => setEditingProduct(null)}
        />
      )}

      <ProductList
        products={products}
        onEdit={setEditingProduct}
        onUpdateInventory={handleUpdateInventory}
      />
    </div>
  );
};

export default FarmerDashboard;

