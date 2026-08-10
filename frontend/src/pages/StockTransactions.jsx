import { useState, useEffect } from 'react';
import axiosClient from '../api/axiosClient';
import { Link } from 'react-router-dom';

export default function StockTransactions() {
  const [products, setProducts] = useState([]);
  const [productId, setProductId] = useState('');
  const [quantity, setQuantity] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [history, setHistory] = useState([]);

  const fetchProducts = async () => {
    try {
      const response = await axiosClient.get('/products');
      setProducts(response.data);
    } catch (err) {
      setError('Failed to load products');
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchHistory = async (id) => {
    try {
      const response = await axiosClient.get(`/inventory-transactions/product/${id}`);
      setHistory(response.data);
    } catch (err) {
      setHistory([]);
    }
  };

  const handleStockIn = async () => {
    setError('');
    setMessage('');
    try {
      await axiosClient.post('/inventory-transactions/stock-in', {
        productId: Number(productId),
        quantity: Number(quantity),
        referencePurchaseOrderId: null,
      });
      setMessage('Stock In recorded successfully');
      setQuantity('');
      fetchProducts();
      if (productId) fetchHistory(productId);
    } catch (err) {
      setError(err.response?.data?.message || 'Stock In failed');
    }
  };

  const handleStockOut = async () => {
    setError('');
    setMessage('');
    try {
      await axiosClient.post('/inventory-transactions/stock-out', {
        productId: Number(productId),
        quantity: Number(quantity),
      });
      setMessage('Stock Out recorded successfully');
      setQuantity('');
      fetchProducts();
      if (productId) fetchHistory(productId);
    } catch (err) {
      setError(err.response?.data?.message || 'Stock Out failed');
    }
  };

  const handleProductChange = (id) => {
    setProductId(id);
    setHistory([]);
    if (id) fetchHistory(id);
  };

  return (
    <div style={{ padding: '20px', maxWidth: '900px' }}>
      <Link to="/dashboard">← Back to Dashboard</Link>
      <h1>Stock Transactions</h1>

      <div style={{ marginBottom: '20px' }}>
        <select
          value={productId}
          onChange={(e) => handleProductChange(e.target.value)}
          style={{ padding: '6px', marginRight: '8px' }}
        >
          <option value="">Select Product</option>
          {products.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name} (Current: {p.currentQuantity})
            </option>
          ))}
        </select>
        <input
          type="number"
          placeholder="Quantity"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
          style={{ padding: '6px', marginRight: '8px', width: '100px' }}
        />
        <button onClick={handleStockIn} disabled={!productId || !quantity}>
          Stock In
        </button>{' '}
        <button onClick={handleStockOut} disabled={!productId || !quantity}>
          Stock Out
        </button>
      </div>

      {message && <p style={{ color: 'green' }}>{message}</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}

      {productId && (
        <>
          <h3>Transaction History</h3>
          <table border="1" cellPadding="8" style={{ borderCollapse: 'collapse', width: '100%' }}>
            <thead>
              <tr>
                <th>Type</th>
                <th>Quantity</th>
                <th>Before</th>
                <th>After</th>
                <th>Status</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {history.map((t) => (
                <tr key={t.id}>
                  <td>{t.type}</td>
                  <td>{t.quantity}</td>
                  <td>{t.quantityBefore}</td>
                  <td>{t.quantityAfter}</td>
                  <td>{t.status}</td>
                  <td>{new Date(t.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
}