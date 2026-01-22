interface TaskStatsProps {
  completed: number;
  pending: number;
  inProgress: number;
}

export default function TaskStats({ completed, pending, inProgress }: TaskStatsProps) {
  const total = completed + pending + inProgress;
  const maxValue = Math.max(completed, pending, inProgress, 1);

  const getBarHeight = (value: number) => {
    return total === 0 ? 0 : (value / maxValue) * 100;
  };

  return (
    <div className="dashboard-card">
      <h3 className="dashboard-card-title">Task Stats</h3>
      <div className="stats-chart">
        <div className="stats-bar-container">
          <div
            className="stats-bar stats-bar-completed"
            style={{ height: `${getBarHeight(completed)}%` }}
          />
          <span className="stats-bar-value">{completed}</span>
          <span className="stats-bar-label">Completed</span>
        </div>
        <div className="stats-bar-container">
          <div
            className="stats-bar stats-bar-progress"
            style={{ height: `${getBarHeight(inProgress)}%` }}
          />
          <span className="stats-bar-value">{inProgress}</span>
          <span className="stats-bar-label">In Progress</span>
        </div>
        <div className="stats-bar-container">
          <div
            className="stats-bar stats-bar-pending"
            style={{ height: `${getBarHeight(pending)}%` }}
          />
          <span className="stats-bar-value">{pending}</span>
          <span className="stats-bar-label">Pending</span>
        </div>
      </div>
    </div>
  );
}
