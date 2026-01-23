import { useState } from 'react';
import TaskItem from './TaskItem';
import AddTaskRow from './AddTaskRow';
import TaskDetailModal from './TaskDetailModal';
import SeriesDeleteModal from './SeriesDeleteModal';
import type { Task, TaskRequest, TaskStatus } from '../../types/api';

export interface TaskForDisplay {
  id: number;
  title: string;
  status: TaskStatus;
}

interface TaskListProps {
  tasks: TaskForDisplay[];
  fullTasks: Task[];
  selectedDate: string;
  onAdd: (title: string) => void;
  onDelete?: (id: number) => void;
  onSeriesExclude?: (seriesId: number, date: string) => void;
  onSeriesStop?: (seriesId: number, date: string) => void;
  onUpdate?: (taskId: number, request: TaskRequest) => void;
}

export default function TaskList({ tasks, fullTasks, selectedDate, onAdd, onDelete, onSeriesExclude, onSeriesStop, onUpdate }: TaskListProps) {
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [seriesDeleteTarget, setSeriesDeleteTarget] = useState<Task | null>(null);

  // Sort: COMPLETED tasks go to bottom
  const sortedTasks = [...tasks].sort((a, b) => {
    if (a.status === 'COMPLETED' && b.status !== 'COMPLETED') return 1;
    if (a.status !== 'COMPLETED' && b.status === 'COMPLETED') return -1;
    return 0;
  });

  const handleTaskClick = (taskId: number) => {
    const task = fullTasks.find((t) => t.id === taskId);
    if (task) {
      setSelectedTask(task);
      setIsModalOpen(true);
    }
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedTask(null);
  };

  const handleSave = (taskId: number, request: TaskRequest) => {
    onUpdate?.(taskId, request);
  };

  const handleDelete = (taskId: number) => {
    const task = fullTasks.find((t) => t.id === taskId);
    if (task?.seriesId) {
      setSeriesDeleteTarget(task);
    } else {
      onDelete?.(taskId);
    }
  };

  const handleSeriesSkip = () => {
    if (seriesDeleteTarget?.seriesId) {
      onSeriesExclude?.(seriesDeleteTarget.seriesId, selectedDate);
    }
    setSeriesDeleteTarget(null);
  };

  const handleSeriesStop = () => {
    if (seriesDeleteTarget?.seriesId) {
      onSeriesStop?.(seriesDeleteTarget.seriesId, selectedDate);
    }
    setSeriesDeleteTarget(null);
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Tasks</h2>
      <div style={styles.list}>
        {sortedTasks.map((task) => (
          <TaskItem
            key={task.id}
            task={task}
            onDelete={handleDelete}
            onClick={handleTaskClick}
          />
        ))}
        <AddTaskRow onAdd={onAdd} />
      </div>
      <TaskDetailModal
        isOpen={isModalOpen}
        task={selectedTask}
        onClose={handleCloseModal}
        onSave={handleSave}
      />
      <SeriesDeleteModal
        isOpen={seriesDeleteTarget !== null}
        onClose={() => setSeriesDeleteTarget(null)}
        onSkip={handleSeriesSkip}
        onStop={handleSeriesStop}
      />
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
  },
  title: {
    margin: '0 0 12px 0',
    fontSize: '18px',
    fontWeight: '600',
  },
  list: {
    flex: 1,
    overflowY: 'auto',
  },
};
