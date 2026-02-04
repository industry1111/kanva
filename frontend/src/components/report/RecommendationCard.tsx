interface RecommendationCardProps {
  content: string;
}

export default function RecommendationCard({ content }: RecommendationCardProps) {
  const lines = content.split('\n').filter((line) => line.trim());

  return (
    <div className="recommendation-card">
      <div className="recommendation-header">
        <span className="recommendation-icon">🎯</span>
        <h4 className="recommendation-title">AI 추천</h4>
      </div>
      <ul className="recommendation-list">
        {lines.map((line, index) => (
          <li key={index} className="recommendation-item">
            {line.replace(/^[•\-]\s*/, '')}
          </li>
        ))}
      </ul>
    </div>
  );
}
