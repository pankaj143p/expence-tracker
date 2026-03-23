# 💰 Smart Multi-Agent AI Expense Management System

A full-stack AI-powered expense manager using Java Spring Boot, React, PostgreSQL, and Ollama (Mistral/LLaMA).

---

## 🏗 Project Structure

```
multi_agent/
├── backend/                  # Spring Boot (Java 17)
│   └── src/main/java/com/expense/
│       ├── agent/            # 5 AI Agents
│       │   ├── OllamaClient.java
│       │   ├── ExtractionAgent.java
│       │   ├── CategorizationAgent.java
│       │   ├── BudgetMonitoringAgent.java
│       │   ├── AnalyticsAgent.java
│       │   └── RecommendationAgent.java
│       ├── controller/       # REST Controllers
│       ├── service/          # Business Logic
│       ├── model/            # JPA Entities
│       ├── repository/       # Spring Data JPA
│       ├── security/         # JWT Auth
│       ├── config/           # Spring Security Config
│       └── dto/              # Request/Response DTOs
├── frontend/                 # React 18
│   └── src/
│       ├── pages/            # Dashboard, Expenses, Budget, Notifications
│       ├── components/       # Navbar
│       ├── services/         # Axios API client
│       └── context/          # Auth context
└── database/
    └── schema.sql            # PostgreSQL schema
```

---

## 🤖 Multi-Agent Architecture

| Agent | Role |
|-------|------|
| **ExtractionAgent** | Parses natural language → amount, category, date |
| **CategorizationAgent** | Validates category, classifies NEED/WANT |
| **BudgetMonitoringAgent** | Checks daily/monthly limits, fires alerts |
| **AnalyticsAgent** | Weekly trends, category breakdown, needs vs wants |
| **RecommendationAgent** | AI-powered saving suggestions via Ollama |

---

## ⚙️ Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+
- [Ollama](https://ollama.ai) (free local AI)

---

## 🚀 Setup Instructions

### 1. Install Ollama + Pull Mistral Model
```bash
# Install Ollama from https://ollama.ai
ollama pull mistral
ollama serve   # starts on http://localhost:11434
```

### 2. PostgreSQL Setup
```bash
psql -U postgres
\i database/schema.sql
```

### 3. Backend Setup
```bash
cd backend

# Edit src/main/resources/application.properties
# Set your DB password: spring.datasource.password=your_password
# Set JWT secret: jwt.secret=your_super_secret_key_min_32_chars

mvn clean install
mvn spring-boot:run
# Runs on http://localhost:8080
```

### 4. Frontend Setup
```bash
cd frontend
npm install
npm start
# Runs on http://localhost:3000
```

---

## 📡 API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |

### Expenses
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/expenses/nl` | Natural language input |
| POST | `/api/expenses` | Manual expense entry |
| GET | `/api/expenses` | Get all expenses |
| DELETE | `/api/expenses/{id}` | Delete expense |
| GET | `/api/expenses/dashboard` | Analytics dashboard |
| GET | `/api/expenses/export/csv` | Export to CSV |

### Budget
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/budget` | Set daily/monthly limits |
| GET | `/api/budget` | Get current budget |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notifications` | All notifications |
| GET | `/api/notifications/unread-count` | Unread count |
| PUT | `/api/notifications/{id}/read` | Mark as read |

---

## 💡 Natural Language Examples

```
"Spent ₹500 on lunch today"
"Paid 1200 for electricity bill"
"Bought groceries for 800 rupees yesterday"
"Movie tickets 600 last Saturday"
"Uber ride 250 this morning"
```

---

## 🔧 Configuration

### Switch AI Model (application.properties)
```properties
# Use Mistral (default)
ollama.model=mistral

# Use LLaMA 3
ollama.model=llama3

# Use CodeGemma
ollama.model=gemma
```

### HuggingFace Fallback (optional)
```properties
huggingface.api-key=hf_your_key_here
```

---

## 🔐 Security Notes

- JWT tokens expire in 24 hours (configurable via `jwt.expiration`)
- Passwords are BCrypt hashed
- All expense endpoints require Bearer token
- CORS restricted to `http://localhost:3000` by default

---

## 📦 Added Beyond Original Prompt

| Feature | Details |
|---------|---------|
| **Regex fallback** | Works without Ollama — keyword-based extraction |
| **NEED/WANT classifier** | Auto-classifies every expense |
| **CSV Export** | Download monthly expenses as CSV |
| **Notification system** | Persistent DB-backed alerts with read/unread state |
| **JWT Auth** | Stateless, production-ready authentication |
| **Budget progress bar** | Visual % used on dashboard |
| **Responsive UI** | Mobile-friendly layout |
| **Proxy config** | React dev server proxies to Spring Boot |
| **Auto-refresh notifications** | Polls every 30s for new alerts |
# expence-tracker
