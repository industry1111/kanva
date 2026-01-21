import { useState } from 'react';

interface DateBadgeProps {
  selectedDate: string;
  onSelectDate: (date: string) => void;
}

function getWeekDates(baseDate: string): string[] {
  const dates: string[] = [];
  const base = new Date(baseDate);
  const dayOfWeek = base.getDay(); // 0 = 일요일

  // 해당 주의 일요일 찾기
  const sunday = new Date(base);
  sunday.setDate(base.getDate() - dayOfWeek);

  // 일요일부터 토요일까지
  for (let i = 0; i < 7; i++) {
    const d = new Date(sunday);
    d.setDate(sunday.getDate() + i);
    dates.push(d.toISOString().split('T')[0]);
  }
  return dates;
}

function formatDay(dateStr: string): { day: number; weekday: string } {
  const date = new Date(dateStr);
  const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
  return {
    day: date.getDate(),
    weekday: weekdays[date.getDay()],
  };
}

function formatWeekLabel(dates: string[]): string {
  const start = new Date(dates[0]);
  const end = new Date(dates[6]);
  const startMonth = start.getMonth() + 1;
  const endMonth = end.getMonth() + 1;

  if (startMonth === endMonth) {
    return `${start.getFullYear()}년 ${startMonth}월`;
  }
  return `${startMonth}월 - ${endMonth}월`;
}

function isToday(dateStr: string): boolean {
  return dateStr === new Date().toISOString().split('T')[0];
}

export default function DateBadge({ selectedDate, onSelectDate }: DateBadgeProps) {
  const [weekBase, setWeekBase] = useState(new Date().toISOString().split('T')[0]);
  const dates = getWeekDates(weekBase);

  const goToPrevWeek = () => {
    const current = new Date(weekBase);
    current.setDate(current.getDate() - 7);
    setWeekBase(current.toISOString().split('T')[0]);
  };

  const goToNextWeek = () => {
    const current = new Date(weekBase);
    current.setDate(current.getDate() + 7);
    setWeekBase(current.toISOString().split('T')[0]);
  };

  const goToToday = () => {
    const today = new Date().toISOString().split('T')[0];
    setWeekBase(today);
    onSelectDate(today);
  };

  return (
    <div className="date-nav-wrapper">
      <div className="date-nav-header">
        <span className="week-label">{formatWeekLabel(dates)}</span>
        <div className="date-nav-buttons">
          <button className="nav-btn" onClick={goToPrevWeek}>←</button>
          <button className="nav-btn today-btn" onClick={goToToday}>오늘</button>
          <button className="nav-btn" onClick={goToNextWeek}>→</button>
        </div>
      </div>
      <div className="date-picker">
        {dates.map((date) => {
          const { day, weekday } = formatDay(date);
          const isSelected = date === selectedDate;
          const isTodayDate = isToday(date);

          return (
            <button
              key={date}
              onClick={() => onSelectDate(date)}
              className={`date-item ${isSelected ? 'selected' : ''} ${isTodayDate ? 'today' : ''}`}
            >
              <span className="date-weekday">{weekday}</span>
              <span className="date-day">{day}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
