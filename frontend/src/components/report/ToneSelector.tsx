import type { ReportTone } from '../../types/report';

interface ToneSelectorProps {
  selectedTone: ReportTone;
  onSelectTone: (tone: ReportTone) => void;
  disabled?: boolean;
}

export default function ToneSelector({
  selectedTone,
  onSelectTone,
  disabled = false,
}: ToneSelectorProps) {
  const tones: { type: ReportTone; label: string; icon: string; description: string }[] = [
    { type: 'ENCOURAGING', label: '격려', icon: '🤗', description: '따뜻하게 응원해줘요' },
    { type: 'STRICT', label: '강압', icon: '🔥', description: '직설적으로 채찍질해요' },
  ];

  const getActiveClasses = (type: ReportTone, isActive: boolean): string => {
    if (!isActive) return 'border-border bg-white text-text-secondary hover:bg-bg';
    if (type === 'ENCOURAGING') return 'bg-bg border-success text-text';
    return 'bg-bg border-danger text-danger';
  };

  return (
    <div className="bg-white rounded-xl p-4 shadow-sm">
      <h3 className="text-[13px] font-semibold text-text mb-1.5">피드백 스타일</h3>
      <div className="flex gap-1.5">
        {tones.map(({ type, label, icon, description }) => (
          <button
            key={type}
            className={`flex-1 flex items-center justify-center gap-1 py-1 px-2 border rounded-lg text-[13px] font-medium cursor-pointer transition-colors disabled:opacity-50 disabled:cursor-not-allowed ${getActiveClasses(type, selectedTone === type)}`}
            onClick={() => onSelectTone(type)}
            disabled={disabled}
            title={description}
          >
            <span className="text-sm">{icon}</span>
            <span className="text-[13px]">{label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
