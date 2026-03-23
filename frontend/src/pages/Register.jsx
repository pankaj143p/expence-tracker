import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function Register() {
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { data } = await authAPI.register(form);
      login(data);
      toast.success('Account created!');
      navigate('/');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1>💰 ExpenseAI</h1>
        <h2>Create Account</h2>
        <form onSubmit={handleSubmit}>
          <input
            type="text" placeholder="Username" required
            value={form.username}
            onChange={e => setForm({ ...form, username: e.target.value })}
          />
          <input
            type="email" placeholder="Email" required
            value={form.email}
            onChange={e => setForm({ ...form, email: e.target.value })}
          />
          <input
            type="password" placeholder="Password (min 6 chars)" required minLength={6}
            value={form.password}
            onChange={e => setForm({ ...form, password: e.target.value })}
          />
          <button type="submit" disabled={loading}>
            {loading ? 'Creating...' : 'Create Account'}
          </button>
        </form>
        <p>Already have an account? <Link to="/login">Sign In</Link></p>
      </div>
    </div>
  );
}
