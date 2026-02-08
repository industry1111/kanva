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

  return (
    <div className="tone-selector">
      <h3 className="tone-selector-title">피드백 스타일</h3>
      <div className="tone-selector-options">
        {tones.map(({ type, label, icon, description }) => (
          <button
            key={type}
            className={`tone-option ${selectedTone === type ? 'active' : ''}`}
            onClick={() => onSelectTone(type)}
            disabled={disabled}
            title={description}
          >
            <span className="tone-option-icon">{icon}</span>
            <span className="tone-option-label">{label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
