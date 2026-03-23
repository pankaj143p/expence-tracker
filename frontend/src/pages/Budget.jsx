import { useState, useEffect } from 'react';
import { budgetAPI } from '../services/api';
import toast from 'react-hot-toast';

export default function Budget() {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());
  const [budget, setBudget] = useState(null);
  const [form, setForm] = useState({ dailyLimit: '', monthlyLimit: '' });

  useEffect(() => {
    budgetAPI.get(month, year)
      .then(({ data }) => {
        if (data) {
          setBudget(data);
          setForm({ dailyLimit: data.dailyLimit || '', monthlyLimit: data.monthlyLimit || '' });
        } else {
          setBudget(null);
          setForm({ dailyLimit: '', monthlyLimit: '' });
        }
      })
      .catch(() => {});
  }, [month, year]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const { data } = await budgetAPI.set({
        dailyLimit: parseFloat(form.dailyLimit),
        monthlyLimit: parseFloat(form.monthlyLimit),
        month, year
      });
      setBudget(data);
      toast.success('Budget saved!');
    } catch {
      toast.error('Failed to save budget');
    }
  };

  return (
    <div className="budget-page">
      <h2>🎯 Budget Settings</h2>

      <div className="month-selector">
        <select value={month} onChange={e => setMonth(+e.target.value)}>
          {Array.from({ length: 12 }, (_, i) => (
            <option key={i + 1} value={i + 1}>
              {new Date(2000, i).toLocaleString('default', { month: 'long' })}
            </option>
          ))}
        </select>
        <input type="number" value={year} min="2020" max="2030"
          onChange={e => setYear(+e.target.value)} />
      </div>

      <div className="budget-card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Daily Spending Limit (₹)</label>
            <input
              type="number" min="1" placeholder="e.g. 800"
              value={form.dailyLimit}
              onChange={e => setForm({ ...form, dailyLimit: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label>Monthly Spending Limit (₹)</label>
            <input
              type="number" min="1" placeholder="e.g. 20000"
              value={form.monthlyLimit}
              onChange={e => setForm({ ...form, monthlyLimit: e.target.value })}
            />
          </div>
          <button type="submit">Save Budget</button>
        </form>

        {budget && (
          <div className="budget-info">
            <h4>Current Budget</h4>
            <p>Daily Limit: <strong>₹{budget.dailyLimit?.toLocaleString()}</strong></p>
            <p>Monthly Limit: <strong>₹{budget.monthlyLimit?.toLocaleString()}</strong></p>
          </div>
        )}
      </div>

      <div className="budget-tips">
        <h3>💡 Budget Tips</h3>
        <ul>
          <li>50/30/20 Rule: 50% Needs, 30% Wants, 20% Savings</li>
          <li>Set daily limits to avoid impulse spending</li>
          <li>Review your budget monthly and adjust as needed</li>
          <li>Track every expense, even small ones — they add up!</li>
        </ul>
      </div>
    </div>
  );
}
