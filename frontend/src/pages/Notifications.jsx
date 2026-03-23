import { useState, useEffect } from 'react';
import { notificationAPI } from '../services/api';
import toast from 'react-hot-toast';

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    notificationAPI.getAll()
      .then(({ data }) => setNotifications(data))
      .catch(() => toast.error('Failed to load notifications'));
  }, []);

  const markRead = async (id) => {
    try {
      await notificationAPI.markRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
    } catch {
      toast.error('Failed to mark as read');
    }
  };

  const typeIcon = (type) => {
    if (type === 'DAILY_LIMIT') return '📅';
    if (type === 'MONTHLY_LIMIT') return '📆';
    return '💡';
  };

  return (
    <div className="notifications-page">
      <h2>🔔 Notifications</h2>
      {notifications.length === 0 ? (
        <div className="empty-state">No notifications yet. Keep tracking your expenses!</div>
      ) : (
        <div className="notification-list">
          {notifications.map(n => (
            <div key={n.id} className={`notification-item ${n.read ? 'read' : 'unread'}`}>
              <span className="notif-icon">{typeIcon(n.type)}</span>
              <div className="notif-content">
                <p>{n.message}</p>
                <small>{new Date(n.createdAt).toLocaleString()}</small>
              </div>
              {!n.read && (
                <button onClick={() => markRead(n.id)} className="btn-mark-read">✓</button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
