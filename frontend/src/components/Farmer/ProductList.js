import React, { useState } from 'react';
import './ProductList.css';

const ProductList = ({ products, onEdit, onUpdateInventory }) => {
  const [inventoryUpdates, setInventoryUpdates] = useState({});

  const handleInventoryChange = (productId, value) => {
    setInventoryUpdates({
      ...inventoryUpdates,
      [productId]: parseFloat(value) || 0,
    });
  };

  const handleInventorySubmit = (productId) => {
    const quantity = inventoryUpdates[productId];
    if (quantity !== undefined && quantity >= 0) {
      onUpdateInventory(productId, quantity);
      setInventoryUpdates({
        ...inventoryUpdates,
        [productId]: undefined,
      });
    }
  };

  if (products.length === 0) {
    return (
      <div className="empty-state">
        <h3>No products yet</h3>
        <p>Add your first product to get started!</p>
      </div>
    );
  }

  return (
    <div className="products-section">
      <h2>My Products</h2>
      <div className="products-grid">
        {products.map((product) => (
          <div key={product.id} className="product-card">
            {product.imageUrl && (
              <img src={product.imageUrl} alt={product.name} />
            )}
            <div className="product-card-body">
              <h3>{product.name}</h3>
              <p className="product-category">{product.category}</p>
              <p className="product-description">{product.description}</p>
              <div className="product-details">
                <div className="product-price">₹{product.pricePerKg}/kg</div>
                <div className="product-quantity">
                  Available: {product.availableQuantity} kg
                </div>
              </div>
              
              <div className="product-actions">
                <button
                  onClick={() => onEdit(product)}
                  className="btn btn-secondary btn-sm"
                >
                  Edit
                </button>
                
                <div className="inventory-update">
                  <input
                    type="number"
                    placeholder="New quantity"
                    value={inventoryUpdates[product.id] || ''}
                    onChange={(e) => handleInventoryChange(product.id, e.target.value)}
                    min="0"
                    step="0.1"
                    className="inventory-input"
                  />
                  <button
                    onClick={() => handleInventorySubmit(product.id)}
                    className="btn btn-primary btn-sm"
                    disabled={inventoryUpdates[product.id] === undefined}
                  >
                    Update
                  </button>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ProductList;

