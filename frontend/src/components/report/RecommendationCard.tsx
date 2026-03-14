interface RecommendationCardProps {
  content: string;
}

export default function RecommendationCard({ content }: RecommendationCardProps) {
  const lines = content.split('\n').filter((line) => line.trim());

  return (
    <div className="bg-bg border border-border rounded-lg p-3">
      <div className="flex items-center gap-1.5 mb-2">
        <span className="text-sm">🎯</span>
        <h4 className="text-[13px] font-semibold text-text m-0">AI 추천</h4>
      </div>
      <ul className="list-none p-0 m-0 flex flex-col gap-1.5">
        {lines.map((line, index) => (
          <li key={index} className="recommendation-arrow text-[13px] text-text-secondary leading-normal">
            {line.replace(/^[•\-]\s*/, '')}
          </li>
        ))}
      </ul>
    </div>
  );
}
