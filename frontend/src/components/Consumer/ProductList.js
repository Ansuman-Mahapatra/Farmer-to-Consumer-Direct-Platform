import React, { useState, useEffect } from 'react';
import { consumerAPI } from '../../services/api';
import { useCart } from '../../context/CartContext';
import './ProductList.css';

const ProductList = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({
    category: '',
    keyword: '',
    minPrice: '',
    maxPrice: '',
    page: 0,
    size: 12,
    sortBy: 'name',
    sortDir: 'asc',
  });
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalItems: 0,
  });
  const { addToCart } = useCart();

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchProducts();
  }, [filters]);

  const fetchCategories = async () => {
    try {
      const response = await consumerAPI.getCategories();
      setCategories(response.data);
    } catch (err) {
      console.error('Failed to fetch categories:', err);
    }
  };

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const params = {
        ...filters,
        page: filters.page,
        size: filters.size,
      };
      
      // Remove empty filters
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null) {
          delete params[key];
        }
      });

      const response = await consumerAPI.browseProducts(params);
      setProducts(response.data.products);
      setPagination({
        currentPage: response.data.currentPage,
        totalPages: response.data.totalPages,
        totalItems: response.data.totalItems,
      });
      setError('');
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to fetch products');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters({
      ...filters,
      [key]: value,
      page: 0, // Reset to first page when filters change
    });
  };

  const handleAddToCart = (product) => {
    if (product.availableQuantity <= 0) {
      alert('This product is out of stock');
      return;
    }
    addToCart(product);
  };

  const handlePageChange = (newPage) => {
    setFilters({
      ...filters,
      page: newPage,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  if (loading && products.length === 0) {
    return <div className="loading">Loading products...</div>;
  }

  return (
    <div className="container">
      <h1>Browse Products</h1>

      <div className="filters-section">
        <div className="filter-group">
          <label>Search</label>
          <input
            type="text"
            placeholder="Search products..."
            value={filters.keyword}
            onChange={(e) => handleFilterChange('keyword', e.target.value)}
            className="filter-input"
          />
        </div>

        <div className="filter-group">
          <label>Category</label>
          <select
            value={filters.category}
            onChange={(e) => handleFilterChange('category', e.target.value)}
            className="filter-select"
          >
            <option value="">All Categories</option>
            {categories.map((cat) => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label>Min Price (₹)</label>
          <input
            type="number"
            placeholder="Min"
            value={filters.minPrice}
            onChange={(e) => handleFilterChange('minPrice', e.target.value)}
            className="filter-input"
            min="0"
          />
        </div>

        <div className="filter-group">
          <label>Max Price (₹)</label>
          <input
            type="number"
            placeholder="Max"
            value={filters.maxPrice}
            onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
            className="filter-input"
            min="0"
          />
        </div>

        <div className="filter-group">
          <label>Sort By</label>
          <select
            value={`${filters.sortBy}-${filters.sortDir}`}
            onChange={(e) => {
              const [sortBy, sortDir] = e.target.value.split('-');
              setFilters({ ...filters, sortBy, sortDir });
            }}
            className="filter-select"
          >
            <option value="name-asc">Name (A-Z)</option>
            <option value="name-desc">Name (Z-A)</option>
            <option value="pricePerKg-asc">Price (Low to High)</option>
            <option value="pricePerKg-desc">Price (High to Low)</option>
          </select>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {products.length === 0 && !loading ? (
        <div className="empty-state">
          <h3>No products found</h3>
          <p>Try adjusting your filters</p>
        </div>
      ) : (
        <>
          <div className="products-grid">
            {products.map((product) => (
              <div key={product.id} className="product-card">
                {product.imageUrl && (
                  <img src={product.imageUrl} alt={product.name} />
                )}
                <div className="product-card-body">
                  <h3>{product.name}</h3>
                  <p className="product-category">{product.category}</p>
                  {product.description && (
                    <p className="product-description">{product.description}</p>
                  )}
                  <div className="product-details">
                    <div className="product-price">₹{product.pricePerKg}/kg</div>
                    <div className="product-quantity">
                      {product.availableQuantity > 0 ? (
                        <span className="in-stock">
                          {product.availableQuantity} kg available
                        </span>
                      ) : (
                        <span className="out-of-stock">Out of stock</span>
                      )}
                    </div>
                  </div>
                  <button
                    onClick={() => handleAddToCart(product)}
                    className="btn btn-primary"
                    disabled={product.availableQuantity <= 0}
                  >
                    {product.availableQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}
                  </button>
                </div>
              </div>
            ))}
          </div>

          {pagination.totalPages > 1 && (
            <div className="pagination">
              <button
                onClick={() => handlePageChange(pagination.currentPage - 1)}
                disabled={pagination.currentPage === 0}
                className="btn btn-secondary"
              >
                Previous
              </button>
              <span className="pagination-info">
                Page {pagination.currentPage + 1} of {pagination.totalPages} 
                ({pagination.totalItems} items)
              </span>
              <button
                onClick={() => handlePageChange(pagination.currentPage + 1)}
                disabled={pagination.currentPage >= pagination.totalPages - 1}
                className="btn btn-secondary"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default ProductList;

