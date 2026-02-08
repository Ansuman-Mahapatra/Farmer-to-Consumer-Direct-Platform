import React, { useState } from 'react';
import './ProductForm.css';

const AddProductForm = ({ onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    name: '',
    category: '',
    pricePerKg: '',
    availableQuantity: '',
    description: '',
    language: 'en',
  });
  const [imageFile, setImageFile] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleImageChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setImageFile(e.target.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const formDataToSend = new FormData();
      formDataToSend.append('name', formData.name);
      formDataToSend.append('category', formData.category);
      formDataToSend.append('pricePerKg', formData.pricePerKg);
      formDataToSend.append('availableQuantity', formData.availableQuantity);
      formDataToSend.append('description', formData.description);
      formDataToSend.append('language', formData.language);
      if (imageFile) {
        formDataToSend.append('image', imageFile);
      }

      await onSubmit(formDataToSend);
      // Reset form
      setFormData({
        name: '',
        category: '',
        pricePerKg: '',
        availableQuantity: '',
        description: '',
        language: 'en',
      });
      setImageFile(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="product-form-container">
      <div className="card">
        <h2>Add New Product</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Product Name *</label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              placeholder="e.g., Organic Tomatoes"
            />
          </div>

          <div className="form-group">
            <label>Category *</label>
            <input
              type="text"
              name="category"
              value={formData.category}
              onChange={handleChange}
              required
              placeholder="e.g., Vegetables, Fruits, Grains"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Price per Kg (₹) *</label>
              <input
                type="number"
                name="pricePerKg"
                value={formData.pricePerKg}
                onChange={handleChange}
                required
                min="0"
                step="0.01"
                placeholder="0.00"
              />
            </div>

            <div className="form-group">
              <label>Available Quantity (kg) *</label>
              <input
                type="number"
                name="availableQuantity"
                value={formData.availableQuantity}
                onChange={handleChange}
                required
                min="0"
                step="0.1"
                placeholder="0.0"
              />
            </div>
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Describe your product..."
              rows="4"
            />
          </div>

          <div className="form-group">
            <label>Product Image</label>
            <input
              type="file"
              accept="image/*"
              onChange={handleImageChange}
            />
            {imageFile && (
              <p className="file-name">Selected: {imageFile.name}</p>
            )}
          </div>

          {error && <div className="error-message">{error}</div>}

          <div className="form-actions">
            <button type="button" onClick={onCancel} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Adding...' : 'Add Product'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddProductForm;

