import { useState } from 'react';
import TaskItem from './TaskItem';
import AddTaskRow from './AddTaskRow';
import TaskDetailModal from './TaskDetailModal';
import type { Task, TaskRequest, TaskStatus } from '../../types/api';

export interface TaskForDisplay {
  id: number;
  title: string;
  status: TaskStatus;
}

interface TaskListProps {
  tasks: TaskForDisplay[];
  fullTasks: Task[];
  onAdd: (title: string) => void;
  onDelete?: (id: number) => void;
  onUpdate?: (taskId: number, request: TaskRequest) => void;
}

export default function TaskList({ tasks, fullTasks, onAdd, onDelete, onUpdate }: TaskListProps) {
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

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

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Tasks</h2>
      <div style={styles.list}>
        {sortedTasks.map((task) => (
          <TaskItem
            key={task.id}
            task={task}
            onDelete={onDelete}
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
