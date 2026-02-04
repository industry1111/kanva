interface InsightCardProps {
  title: string;
  content: string;
  icon?: string;
}

export default function InsightCard({ title, content, icon = '💡' }: InsightCardProps) {
  const lines = content.split('\n').filter((line) => line.trim());

  return (
    <div className="insight-card">
      <div className="insight-card-header">
        <span className="insight-icon">{icon}</span>
        <h4 className="insight-title">{title}</h4>
      </div>
      <div className="insight-content">
        {lines.map((line, index) => (
          <p key={index} className="insight-line">
            {line}
          </p>
        ))}
      </div>
    </div>
  );
}
