import { useState, useEffect } from 'react';
import { expenseAPI } from '../services/api';
import toast from 'react-hot-toast';

const CATEGORIES = ['Food', 'Transport', 'Shopping', 'Entertainment', 'Health', 'Utilities', 'Education', 'Other'];

export default function Expenses() {
  const [expenses, setExpenses] = useState([]);
  const [nlText, setNlText] = useState('');
  const [nlLoading, setNlLoading] = useState(false);
  const [showManual, setShowManual] = useState(false);
  const [form, setForm] = useState({ amount: '', category: 'Food', description: '', expenseDate: new Date().toISOString().split('T')[0] });

  const loadExpenses = () => {
    expenseAPI.getAll().then(({ data }) => setExpenses(data)).catch(() => {});
  };

  useEffect(() => { loadExpenses(); }, []);

  const handleNL = async (e) => {
    e.preventDefault();
    if (!nlText.trim()) return;
    setNlLoading(true);
    try {
      const { data } = await expenseAPI.processNL(nlText);
      toast.success(`Added: ₹${data.amount} - ${data.category}`);
      setNlText('');
      loadExpenses();
    } catch {
      toast.error('Failed to process input');
    } finally {
      setNlLoading(false);
    }
  };

  const handleManual = async (e) => {
    e.preventDefault();
    try {
      await expenseAPI.addManual({ ...form, amount: parseFloat(form.amount) });
      toast.success('Expense added!');
      setForm({ amount: '', category: 'Food', description: '', expenseDate: new Date().toISOString().split('T')[0] });
      setShowManual(false);
      loadExpenses();
    } catch {
      toast.error('Failed to add expense');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this expense?')) return;
    try {
      await expenseAPI.delete(id);
      toast.success('Deleted');
      setExpenses(prev => prev.filter(e => e.id !== id));
    } catch {
      toast.error('Delete failed');
    }
  };

  const handleExport = async () => {
    const now = new Date();
    try {
      const { data } = await expenseAPI.exportCsv(now.getMonth() + 1, now.getFullYear());
      const url = URL.createObjectURL(new Blob([data]));
      const a = document.createElement('a');
      a.href = url; a.download = 'expenses.csv'; a.click();
    } catch {
      toast.error('Export failed');
    }
  };

  return (
    <div className="expenses-page">
      <div className="page-header">
        <h2>💸 Expenses</h2>
        <button onClick={handleExport} className="btn-secondary">📥 Export CSV</button>
      </div>

      {/* Natural Language Input */}
      <div className="nl-section">
        <h3>🤖 Add via Natural Language</h3>
        <form onSubmit={handleNL} className="nl-form">
          <input
            type="text"
            placeholder='e.g. "Spent ₹500 on lunch today" or "Paid 1200 for electricity bill"'
            value={nlText}
            onChange={e => setNlText(e.target.value)}
          />
          <button type="submit" disabled={nlLoading}>
            {nlLoading ? '⏳ Processing...' : '✨ Add'}
          </button>
        </form>
        <button className="btn-link" onClick={() => setShowManual(!showManual)}>
          {showManual ? '▲ Hide manual form' : '▼ Add manually instead'}
        </button>
      </div>

      {/* Manual Form */}
      {showManual && (
        <div className="manual-form-card">
          <form onSubmit={handleManual}>
            <div className="form-row">
              <input
                type="number" placeholder="Amount (₹)" required min="1"
                value={form.amount}
                onChange={e => setForm({ ...form, amount: e.target.value })}
              />
              <select value={form.category} onChange={e => setForm({ ...form, category: e.target.value })}>
                {CATEGORIES.map(c => <option key={c}>{c}</option>)}
              </select>
            </div>
            <div className="form-row">
              <input
                type="text" placeholder="Description"
                value={form.description}
                onChange={e => setForm({ ...form, description: e.target.value })}
              />
              <input
                type="date" value={form.expenseDate}
                onChange={e => setForm({ ...form, expenseDate: e.target.value })}
              />
            </div>
            <button type="submit">Add Expense</button>
          </form>
        </div>
      )}

      {/* Expense List */}
      <div className="expense-list">
        {expenses.length === 0 ? (
          <div className="empty-state">No expenses yet. Add your first one above!</div>
        ) : (
          expenses.map(e => (
            <div key={e.id} className="expense-item">
              <div className="expense-left">
                <span className={`category-badge cat-${e.category?.toLowerCase()}`}>{e.category}</span>
                <div>
                  <div className="expense-desc">{e.description || e.rawInput || '-'}</div>
                  <div className="expense-date">{e.expenseDate}</div>
                </div>
              </div>
              <div className="expense-right">
                <span className="expense-amount">₹{parseFloat(e.amount).toLocaleString()}</span>
                <span className={`type-badge ${e.expenseType?.toLowerCase()}`}>{e.expenseType}</span>
                <button onClick={() => handleDelete(e.id)} className="btn-delete">🗑</button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
