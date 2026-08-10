import { useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';
import { Link } from 'react-router-dom';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [sku, setSku] = useState('');
  const [name, setName] = useState('');
  const [unit, setUnit] = useState('');
  const [minStockThreshold, setMinStockThreshold] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [error, setError] = useState('');

  const fetchProducts = async () => {
    try {
      const response = await axiosClient.get('/products');
      setProducts(response.data);
    } catch (err) {
      setError('Failed to load products');
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await axiosClient.get('/categories');
      setCategories(response.data);
    } catch (err) {
      setError('Failed to load categories');
    }
  };

  useEffect(() => {
    fetchProducts();
    fetchCategories();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await axiosClient.post(`/products?categoryId=${categoryId}`, {
        sku,
        name,
        unit,
        minStockThreshold: Number(minStockThreshold),
      });
      setSku('');
      setName('');
      setUnit('');
      setMinStockThreshold('');
      setCategoryId('');
      fetchProducts();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create product');
    }
  };

  const handleDeactivate = async (id) => {
    try {
      await axiosClient.patch(`/products/${id}/deactivate`);
      fetchProducts();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to deactivate product');
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '900px' }}>
      <Link to="/dashboard">← Back to Dashboard</Link>
      <h1>Products</h1>

      <form onSubmit={handleCreate} style={{ marginBottom: '24px' }}>
        <h3>Add Product</h3>
        <input
          type="text"
          placeholder="SKU"
          value={sku}
          onChange={(e) => setSku(e.target.value)}
          required
          style={{ padding: '6px', marginRight: '8px' }}
        />
        <input
          type="text"
          placeholder="Name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          style={{ padding: '6px', marginRight: '8px' }}
        />
        <input
          type="text"
          placeholder="Unit (e.g. pcs)"
          value={unit}
          onChange={(e) => setUnit(e.target.value)}
          required
          style={{ padding: '6px', marginRight: '8px' }}
        />
        <input
          type="number"
          placeholder="Min Stock"
          value={minStockThreshold}
          onChange={(e) => setMinStockThreshold(e.target.value)}
          required
          style={{ padding: '6px', marginRight: '8px', width: '100px' }}
        />
        <select
          value={categoryId}
          onChange={(e) => setCategoryId(e.target.value)}
          required
          style={{ padding: '6px', marginRight: '8px' }}
        >
          <option value="">Select Category</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </select>
        <button type="submit">Add</button>
      </form>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <table border="1" cellPadding="8" style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th>SKU</th>
            <th>Name</th>
            <th>Category</th>
            <th>Unit</th>
            <th>Current Qty</th>
            <th>Min Stock</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id}>
              <td>{p.sku}</td>
              <td>{p.name}</td>
              <td>{p.category?.name}</td>
              <td>{p.unit}</td>
              <td style={{ color: p.currentQuantity < p.minStockThreshold ? 'red' : 'inherit' }}>
                {p.currentQuantity}
              </td>
              <td>{p.minStockThreshold}</td>
              <td>
                <button onClick={() => handleDeactivate(p.id)}>Deactivate</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}