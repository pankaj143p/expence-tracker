import { useState, useEffect } from 'react';
import { Doughnut, Bar, Line } from 'react-chartjs-2';
import {
  Chart as ChartJS, ArcElement, Tooltip, Legend,
  CategoryScale, LinearScale, BarElement, PointElement, LineElement
} from 'chart.js';
import { expenseAPI } from '../services/api';
import toast from 'react-hot-toast';

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, PointElement, LineElement);

const CHART_COLORS = ['#6366f1','#f59e0b','#10b981','#ef4444','#3b82f6','#8b5cf6','#ec4899','#14b8a6'];

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());

  useEffect(() => {
    setLoading(true);
    expenseAPI.getDashboard(month, year)
      .then(({ data }) => setData(data))
      .catch(() => toast.error('Failed to load dashboard'))
      .finally(() => setLoading(false));
  }, [month, year]);

  if (loading) return <div className="loading">Loading dashboard...</div>;
  if (!data) return null;

  const categoryLabels = Object.keys(data.categoryBreakdown || {});
  const categoryValues = Object.values(data.categoryBreakdown || {});

  const doughnutData = {
    labels: categoryLabels,
    datasets: [{ data: categoryValues, backgroundColor: CHART_COLORS }]
  };

  const weeklyData = {
    labels: (data.weeklyTrend || []).map(d => d.date),
    datasets: [{
      label: 'Daily Spend (₹)',
      data: (data.weeklyTrend || []).map(d => d.amount),
      borderColor: '#6366f1',
      backgroundColor: 'rgba(99,102,241,0.1)',
      fill: true,
      tension: 0.4
    }]
  };

  const needsWantsData = {
    labels: Object.keys(data.needsVsWants || {}),
    datasets: [{
      data: Object.values(data.needsVsWants || {}),
      backgroundColor: ['#10b981', '#f59e0b']
    }]
  };

  const budgetUsed = data.monthlyLimit
    ? Math.min((data.totalThisMonth / data.monthlyLimit) * 100, 100).toFixed(0)
    : null;

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>📊 Dashboard</h2>
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
      </div>

      {/* Summary Cards */}
      <div className="cards-grid">
        <div className="card card-blue">
          <div className="card-label">Today's Spend</div>
          <div className="card-value">₹{(data.totalToday || 0).toLocaleString()}</div>
          {data.dailyLimit && (
            <div className="card-sub">Limit: ₹{data.dailyLimit.toLocaleString()}</div>
          )}
        </div>
        <div className="card card-purple">
          <div className="card-label">This Month</div>
          <div className="card-value">₹{(data.totalThisMonth || 0).toLocaleString()}</div>
          {data.monthlyLimit && (
            <div className="card-sub">Limit: ₹{data.monthlyLimit.toLocaleString()}</div>
          )}
        </div>
        {budgetUsed && (
          <div className={`card ${+budgetUsed >= 100 ? 'card-red' : +budgetUsed >= 80 ? 'card-orange' : 'card-green'}`}>
            <div className="card-label">Budget Used</div>
            <div className="card-value">{budgetUsed}%</div>
            <div className="progress-bar">
              <div className="progress-fill" style={{ width: `${budgetUsed}%` }} />
            </div>
          </div>
        )}
      </div>

      {/* Alerts */}
      {data.alerts?.length > 0 && (
        <div className="alerts-section">
          {data.alerts.map((alert, i) => (
            <div key={i} className="alert-item">{alert}</div>
          ))}
        </div>
      )}

      {/* Charts */}
      <div className="charts-grid">
        {categoryLabels.length > 0 && (
          <div className="chart-card">
            <h3>Category Breakdown</h3>
            <Doughnut data={doughnutData} options={{ plugins: { legend: { position: 'bottom' } } }} />
          </div>
        )}
        {data.weeklyTrend?.length > 0 && (
          <div className="chart-card">
            <h3>7-Day Trend</h3>
            <Line data={weeklyData} options={{ scales: { y: { beginAtZero: true } } }} />
          </div>
        )}
        {Object.keys(data.needsVsWants || {}).length > 0 && (
          <div className="chart-card">
            <h3>Needs vs Wants</h3>
            <Doughnut data={needsWantsData} options={{ plugins: { legend: { position: 'bottom' } } }} />
          </div>
        )}
      </div>

      {/* AI Suggestions */}
      {data.suggestions?.length > 0 && (
        <div className="suggestions-section">
          <h3>🧠 AI Suggestions</h3>
          {data.suggestions.map((s, i) => (
            <div key={i} className="suggestion-item">{s}</div>
          ))}
        </div>
      )}
    </div>
  );
}
