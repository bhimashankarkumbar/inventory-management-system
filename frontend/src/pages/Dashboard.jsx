import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';

export default function Dashboard() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Dashboard</h1>
      <p>You are logged in.</p>
      <nav style={{ marginBottom: '20px', display: 'flex', gap: '16px' }}>
        <Link to="/categories">Manage Categories</Link>
        <Link to="/products">Manage Products</Link>
        <Link to="/stock-transactions">Stock Transactions</Link>
      </nav>
      <button onClick={handleLogout}>Log Out</button>
    </div>
  );
}