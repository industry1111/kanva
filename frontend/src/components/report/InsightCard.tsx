interface InsightCardProps {
  title: string;
  content: string;
  icon?: string;
}

export default function InsightCard({ title, content, icon = '💡' }: InsightCardProps) {
  const lines = content.split('\n').filter((line) => line.trim());

  return (
    <div className="bg-bg border border-border rounded-lg p-3">
      <div className="flex items-center gap-1.5 mb-2">
        <span className="text-sm">{icon}</span>
        <h4 className="text-[13px] font-semibold text-text m-0">{title}</h4>
      </div>
      <div className="flex flex-col gap-1.5">
        {lines.map((line, index) => (
          <p key={index} className="text-[13px] text-text-secondary leading-normal m-0">
            {line}
          </p>
        ))}
      </div>
    </div>
  );
}
