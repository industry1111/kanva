import type { ReportPeriodType } from '../../types/report';

interface PeriodSelectorProps {
  selectedPeriod: ReportPeriodType;
  onSelectPeriod: (period: ReportPeriodType) => void;
  disabled?: boolean;
}

export default function PeriodSelector({
  selectedPeriod,
  onSelectPeriod,
  disabled = false,
}: PeriodSelectorProps) {
  const periods: { type: ReportPeriodType; label: string }[] = [
    { type: 'WEEKLY', label: '주간' },
    { type: 'MONTHLY', label: '월간' },
  ];

  return (
    <div className="period-selector">
      <h3 className="period-selector-title">기간 선택</h3>
      <div className="period-selector-options">
        {periods.map(({ type, label }) => (
          <button
            key={type}
            className={`period-option ${selectedPeriod === type ? 'active' : ''}`}
            onClick={() => onSelectPeriod(type)}
            disabled={disabled}
          >
            {label}
          </button>
        ))}
      </div>
    </div>
  );
}
