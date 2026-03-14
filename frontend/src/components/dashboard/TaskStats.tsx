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
    <div className="bg-white rounded-xl p-4 shadow-sm border border-border">
      <h3 className="text-sm font-semibold text-text mb-3 pb-2 border-b border-border">Task Stats</h3>
      <div className="flex items-end justify-around gap-4 h-40">
        <div className="flex flex-col items-center gap-1 flex-1">
          <div className="w-full flex justify-center items-end h-28">
            <div
              className="w-10 rounded-t-md bg-primary transition-all"
              style={{ height: `${getBarHeight(completed)}%` }}
            />
          </div>
          <span className="text-sm font-bold text-text">{completed}</span>
          <span className="text-xs text-text-secondary">Completed</span>
        </div>
        <div className="flex flex-col items-center gap-1 flex-1">
          <div className="w-full flex justify-center items-end h-28">
            <div
              className="w-10 rounded-t-md bg-blue-400 transition-all"
              style={{ height: `${getBarHeight(inProgress)}%` }}
            />
          </div>
          <span className="text-sm font-bold text-text">{inProgress}</span>
          <span className="text-xs text-text-secondary">In Progress</span>
        </div>
        <div className="flex flex-col items-center gap-1 flex-1">
          <div className="w-full flex justify-center items-end h-28">
            <div
              className="w-10 rounded-t-md bg-border transition-all"
              style={{ height: `${getBarHeight(pending)}%` }}
            />
          </div>
          <span className="text-sm font-bold text-text">{pending}</span>
          <span className="text-xs text-text-secondary">Pending</span>
        </div>
      </div>
    </div>
  );
}
