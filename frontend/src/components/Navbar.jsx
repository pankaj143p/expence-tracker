import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { notificationAPI } from '../services/api';

export default function Navbar() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    if (user) {
      notificationAPI.getUnreadCount()
        .then(({ data }) => setUnread(data.count))
        .catch(() => {});
      const interval = setInterval(() => {
        notificationAPI.getUnreadCount()
          .then(({ data }) => setUnread(data.count))
          .catch(() => {});
      }, 30000);
      return () => clearInterval(interval);
    }
  }, [user]);

  const isActive = (path) => location.pathname === path ? 'active' : '';

  return (
    <nav className="navbar">
      <div className="nav-brand">💰 ExpenseAI</div>
      <div className="nav-links">
        <Link to="/" className={isActive('/')}>Dashboard</Link>
        <Link to="/expenses" className={isActive('/expenses')}>Expenses</Link>
        <Link to="/budget" className={isActive('/budget')}>Budget</Link>
        <Link to="/notifications" className={isActive('/notifications')}>
          🔔 {unread > 0 && <span className="badge">{unread}</span>}
        </Link>
        <span className="nav-user">👤 {user?.username}</span>
        <button onClick={logout} className="btn-logout">Logout</button>
      </div>
    </nav>
  );
}
