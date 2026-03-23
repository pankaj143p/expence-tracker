import { useState, useRef, useEffect } from 'react';
import api from '../services/api';

const SUGGESTIONS = [
  "How much did I spend today?",
  "Give me saving tips",
  "How to set a budget?",
  "What is 50/30/20 rule?",
];

export default function ChatAssistant() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([
    { role: 'assistant', text: "👋 Hi! I'm your ExpenseAI Assistant. Ask me anything about your finances!" }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = async (text) => {
    const msg = text || input.trim();
    if (!msg) return;

    setMessages(prev => [...prev, { role: 'user', text: msg }]);
    setInput('');
    setLoading(true);

    try {
      const { data } = await api.post('/assistant/chat', { message: msg });
      setMessages(prev => [...prev, { role: 'assistant', text: data.reply }]);
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', text: '⚠️ Sorry, I could not respond. Try again.' }]);
    } finally {
      setLoading(false);
    }
  };

  const handleKey = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); }
  };

  return (
    <>
      {/* Floating Button */}
      <button className="chat-fab" onClick={() => setOpen(!open)} title="AI Assistant">
        {open ? '✕' : '🤖'}
      </button>

      {/* Chat Window */}
      {open && (
        <div className="chat-window">
          <div className="chat-header">
            <span>🤖 ExpenseAI Assistant</span>
            <button onClick={() => setOpen(false)}>✕</button>
          </div>

          <div className="chat-messages">
            {messages.map((m, i) => (
              <div key={i} className={`chat-bubble ${m.role}`}>
                {m.text}
              </div>
            ))}
            {loading && (
              <div className="chat-bubble assistant">
                <span className="typing">●●●</span>
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          {/* Quick suggestions */}
          {messages.length <= 1 && (
            <div className="chat-suggestions">
              {SUGGESTIONS.map((s, i) => (
                <button key={i} onClick={() => sendMessage(s)}>{s}</button>
              ))}
            </div>
          )}

          <div className="chat-input-row">
            <input
              type="text"
              placeholder="Ask anything about your finances..."
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKey}
              disabled={loading}
            />
            <button onClick={() => sendMessage()} disabled={loading || !input.trim()}>
              ➤
            </button>
          </div>
        </div>
      )}
    </>
  );
}
