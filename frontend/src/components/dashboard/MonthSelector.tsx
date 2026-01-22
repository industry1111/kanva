interface MonthSelectorProps {
  selectedMonth: string; // 'YYYY-MM' format
  onSelectMonth: (month: string) => void;
}

export default function MonthSelector({ selectedMonth, onSelectMonth }: MonthSelectorProps) {
  // Generate 5 months: selected month in center, 2 before, 2 after
  const getMonthsRange = (): string[] => {
    const [year, month] = selectedMonth.split('-').map(Number);
    const months: string[] = [];

    for (let i = -2; i <= 2; i++) {
      const date = new Date(year, month - 1 + i, 1);
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      months.push(`${y}-${m}`);
    }

    return months;
  };

  const formatShortMonth = (monthStr: string): string => {
    const [, month] = monthStr.split('-').map(Number);
    return `${month}월`;
  };

  const isCurrentMonth = (monthStr: string): boolean => {
    const today = new Date();
    const current = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
    return monthStr === current;
  };

  const months = getMonthsRange();

  return (
    <div className="dashboard-card month-selector">
      <h3 className="dashboard-card-title">월 선택</h3>
      <div className="month-list">
        {months.map((month) => (
          <button
            key={month}
            className={`month-item ${month === selectedMonth ? 'selected' : ''} ${isCurrentMonth(month) ? 'current' : ''}`}
            onClick={() => onSelectMonth(month)}
          >
            <span className="month-year">{month.split('-')[0]}</span>
            <span className="month-name">{formatShortMonth(month)}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
