import { useState, useEffect } from 'react';

interface DailyProductivity {
  date: string;
  completed: number;
  total: number;
}

interface ProductivityChartProps {
  data: DailyProductivity[];
  selectedMonth: string;
}

export default function ProductivityChart({ data, selectedMonth }: ProductivityChartProps) {
  const [startIndex, setStartIndex] = useState(0);
  const VISIBLE_DAYS = 7;

  // Reset to current date position when month changes
  useEffect(() => {
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];

    // Find today's index in data, or last day if not found
    let todayIndex = data.findIndex((d) => d.date === todayStr);

    if (todayIndex === -1) {
      // If today is not in the data (different month), show last 7 days
      todayIndex = data.length - 1;
    }

    // Center the current date (show 3 days before and 3 days after)
    const newStartIndex = Math.max(0, Math.min(todayIndex - 3, data.length - VISIBLE_DAYS));
    setStartIndex(newStartIndex);
  }, [data, selectedMonth]);

  const visibleData = data.slice(startIndex, startIndex + VISIBLE_DAYS);
  const maxTotal = Math.max(...data.map((d) => d.total), 1);

  const getBarHeight = (value: number) => {
    return (value / maxTotal) * 100;
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return `${date.getDate()}`;
  };

  const formatWeekday = (dateStr: string) => {
    const date = new Date(dateStr);
    const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
    return weekdays[date.getDay()];
  };

  const isToday = (dateStr: string) => {
    const today = new Date().toISOString().split('T')[0];
    return dateStr === today;
  };

  const canGoPrev = startIndex > 0;
  const canGoNext = startIndex + VISIBLE_DAYS < data.length;

  const handlePrev = () => {
    setStartIndex(Math.max(0, startIndex - VISIBLE_DAYS));
  };

  const handleNext = () => {
    setStartIndex(Math.min(data.length - VISIBLE_DAYS, startIndex + VISIBLE_DAYS));
  };

  return (
    <div className="dashboard-card productivity-card">
      <div className="productivity-header">
        <h3 className="dashboard-card-title">Productivity Chart</h3>
        <div className="productivity-nav">
          <button
            className="productivity-nav-btn"
            onClick={handlePrev}
            disabled={!canGoPrev}
          >
            ◀
          </button>
          <button
            className="productivity-nav-btn"
            onClick={handleNext}
            disabled={!canGoNext}
          >
            ▶
          </button>
        </div>
      </div>

      <div className="productivity-chart-bar">
        <div className="chart-bars-container">
          {visibleData.map((d) => (
            <div key={d.date} className={`chart-bar-group ${isToday(d.date) ? 'today' : ''}`}>
              <div className="chart-bar-wrapper">
                <div
                  className="chart-bar chart-bar-total"
                  style={{ height: `${getBarHeight(d.total)}%` }}
                  title={`전체: ${d.total}`}
                >
                  {d.total > 0 && <span className="bar-value">{d.total}</span>}
                </div>
                <div
                  className="chart-bar chart-bar-completed"
                  style={{ height: `${getBarHeight(d.completed)}%` }}
                  title={`완료: ${d.completed}`}
                >
                  {d.completed > 0 && <span className="bar-value">{d.completed}</span>}
                </div>
              </div>
              <div className="chart-bar-label">
                <span className="chart-bar-day">{formatDate(d.date)}</span>
                <span className="chart-bar-weekday">{formatWeekday(d.date)}</span>
              </div>
            </div>
          ))}
        </div>

        <div className="chart-legend">
          <div className="legend-item">
            <span className="legend-color legend-total" />
            <span className="legend-text">전체</span>
          </div>
          <div className="legend-item">
            <span className="legend-color legend-completed" />
            <span className="legend-text">완료</span>
          </div>
        </div>
      </div>
    </div>
  );
}
