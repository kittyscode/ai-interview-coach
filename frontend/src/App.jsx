import { useState, useEffect } from "react";
import "./App.css";

const API = "http://localhost:8081";

export default function App() {
  const [topic, setTopic] = useState("Spring Boot");
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [result, setResult] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [difficulty, setDifficulty] = useState("medium");
  const loadHistory = async () => {
    try {
      const res = await fetch(`${API}/history`);
      setHistory(await res.json());
    } catch {
      setError("Cannot reach the server. Is the Spring Boot app running?");
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  const getQuestion = async () => {
    setLoading(true);
    setError("");
    setResult(null);
    setAnswer("");
    try {
      const res = await fetch(
        `${API}/question?topic=${encodeURIComponent(topic)}&difficulty=${difficulty}`,
      );
      setQuestion(await res.text());
    } catch {
      setError("Could not get a question.");
    }
    setLoading(false);
  };

  const submitAnswer = async () => {
    if (!question || !answer.trim()) return;
    setLoading(true);
    setError("");
    try {
      const res = await fetch(`${API}/evaluate`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ question, answer }),
      });
      setResult(await res.json());
      loadHistory();
    } catch {
      setError("Could not evaluate your answer.");
    }
    setLoading(false);
  };

  return (
    <div className="container">
      <h1>AI Interview Coach</h1>

      <div className="row">
        <input
          value={topic}
          onChange={(e) => setTopic(e.target.value)}
          placeholder="Topic, e.g. Spring Boot"
        />
        <select value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>
          <option value="easy">Easy</option>
          <option value="medium">Medium</option>
          <option value="hard">Hard</option>
          
        </select>

        <button onClick={getQuestion} disabled={loading}>
          Get Question
        </button>
      </div>

      {error && <p className="error">{error}</p>}

      {question && (
        <div className="card">
          <h3>Question</h3>
          <p>{question}</p>
          <textarea
            rows="6"
            value={answer}
            onChange={(e) => setAnswer(e.target.value)}
            placeholder="Say your answer out loud first, then type what you said..."
          />
          <button onClick={submitAnswer} disabled={loading}>
            {loading ? "Evaluating..." : "Submit Answer"}
          </button>
        </div>
      )}

      {result && (
        <div className="card result">
          <h3>Score: {result.score} / 10</h3>
          <h4>Strengths</h4>
          <ul>
            {result.strengths?.map((s, i) => (
              <li key={i}>{s}</li>
            ))}
          </ul>
          <h4>Improvements</h4>
          <ul>
            {result.improvements?.map((s, i) => (
              <li key={i}>{s}</li>
            ))}
          </ul>
          <h4>Better answer (read this aloud twice)</h4>
          <p>{result.betterAnswer}</p>
        </div>
      )}

      <h2>Your history</h2>
      {history.length === 0 && <p>No attempts yet.</p>}
      {history.map((h) => (
        <div className="history" key={h.id}>
          <strong>{h.score}/10</strong> {h.question}
          <small> {new Date(h.createdAt).toLocaleString()}</small>
        </div>
      ))}
    </div>
  );
}
