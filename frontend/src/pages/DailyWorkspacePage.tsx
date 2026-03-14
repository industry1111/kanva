import { useState, useEffect, useRef, useCallback } from 'react';
import DateBadge from '../components/header/DateBadge';
import DailyNoteEditor from '../components/note/DailyNoteEditor';
import type { DailyNoteEditorRef } from '../components/note/DailyNoteEditor';
import TaskList from '../components/tasks/TaskList';
import { useAuth } from '../contexts/AuthContext';
import { dailyNoteApi, taskApi, taskSeriesApi } from '../services/api';
import type { Task, DailyNote, TaskRequest } from '../types/api';

function getToday(): string {
  return new Date().toISOString().split('T')[0];
}

export default function DailyWorkspacePage() {
  const { user, logout } = useAuth();
  const [selectedDate, setSelectedDate] = useState(getToday());
  const [dailyNote, setDailyNote] = useState<DailyNote | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const noteEditorRef = useRef<DailyNoteEditorRef>(null);

  // Load data for selected date
  const loadData = useCallback(async (date: string) => {
    setIsLoading(true);
    setError(null);

    try {
      const [noteResponse, tasksResponse] = await Promise.all([
        dailyNoteApi.getByDate(date),
        taskApi.getByDate(date),
      ]);

      if (noteResponse.success) {
        setDailyNote(noteResponse.data);
      }

      if (tasksResponse.success) {
        setTasks(tasksResponse.data);
      }
    } catch (err) {
      setError('데이터를 불러오는데 실패했습니다.');
      console.error('Failed to load data:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData(selectedDate);
  }, [selectedDate, loadData]);

  const handleNoteSave = async (content: string) => {
    try {
      const response = await dailyNoteApi.update(selectedDate, {
        date: selectedDate,
        content,
      });

      if (response.success) {
        setDailyNote(response.data);
      }
    } catch (err) {
      console.error('Failed to save note:', err);
      alert('노트 저장에 실패했습니다.');
    }
  };

  const handleDateChange = (date: string) => {
    if (date === selectedDate) return;

    // Save dirty content before changing date
    try {
      noteEditorRef.current?.saveIfDirty();
    } catch {
      alert('저장 실패: 다시 시도해주세요.');
      return;
    }

    setSelectedDate(date);
  };

  const handleAddTask = async (title: string) => {
    try {
      const response = await taskApi.create(selectedDate, {
        title,
        status: 'PENDING',
      });

      if (response.success) {
        setTasks((prev) => [...prev, response.data]);
      }
    } catch (err) {
      console.error('Failed to add task:', err);
      alert('할 일 추가에 실패했습니다.');
    }
  };

  const handleDeleteTask = async (taskId: number) => {
    try {
      await taskApi.delete(taskId);
      setTasks((prev) => prev.filter((task) => task.id !== taskId));
    } catch (err) {
      console.error('Failed to delete task:', err);
      alert('할 일 삭제에 실패했습니다.');
    }
  };

  const handleUpdateTask = async (taskId: number, request: TaskRequest) => {
    try {
      const response = await taskApi.update(taskId, request);

      if (response.success) {
        setTasks((prev) =>
          prev.map((task) => (task.id === taskId ? response.data : task))
        );
      }
    } catch (err) {
      console.error('Failed to update task:', err);
      alert('할 일 수정에 실패했습니다.');
    }
  };

  const handleSeriesExclude = async (seriesId: number, date: string) => {
    try {
      await taskSeriesApi.excludeDate(seriesId, date);
      setTasks((prev) => prev.filter((task) => !(task.seriesId === seriesId)));
    } catch (err) {
      console.error('Failed to exclude date:', err);
      alert('날짜 제외에 실패했습니다.');
    }
  };

  const handleSeriesStop = async (seriesId: number, date: string) => {
    try {
      await taskSeriesApi.stopSeries(seriesId, date);
      setTasks((prev) => prev.filter((task) => !(task.seriesId === seriesId)));
    } catch (err) {
      console.error('Failed to stop series:', err);
      alert('시리즈 중단에 실패했습니다.');
    }
  };

  const handleToggleTask = async (taskId: number) => {
    try {
      const response = await taskApi.toggle(taskId);
      if (response.success) {
        setTasks((prev) =>
          prev.map((task) => (task.id === taskId ? response.data : task))
        );
      }
    } catch (err) {
      console.error('Failed to toggle task:', err);
    }
  };

  // Convert API Task to component format
  const tasksForComponent = tasks.map((task) => ({
    id: task.id,
    title: task.title,
    status: task.status,
  }));

  const renderContent = () => {
    if (isLoading) {
      return <div className="flex items-center justify-center flex-1 text-sm text-text-secondary">로딩 중...</div>;
    }

    if (error) {
      return (
        <div className="flex flex-col items-center justify-center flex-1 gap-4 text-danger">
          <p>{error}</p>
          <button onClick={() => loadData(selectedDate)} className="px-3 py-1.5 bg-primary text-white border-none rounded-md cursor-pointer text-sm">
            다시 시도
          </button>
        </div>
      );
    }

    return (
      <main className="grid grid-cols-5 gap-6 flex-1 min-h-0">
        <div className="col-span-3 flex flex-col min-w-0 bg-white border border-border rounded-xl p-4">
          <DailyNoteEditor
            ref={noteEditorRef}
            content={dailyNote?.content || ''}
            onSave={handleNoteSave}
          />
        </div>
        <div className="col-span-2 flex flex-col min-w-0 bg-white border border-border rounded-xl p-4">
          <TaskList
            tasks={tasksForComponent}
            fullTasks={tasks}
            selectedDate={selectedDate}
            onAdd={handleAddTask}
            onDelete={handleDeleteTask}
            onSeriesExclude={handleSeriesExclude}
            onSeriesStop={handleSeriesStop}
            onUpdate={handleUpdateTask}
            onToggle={handleToggleTask}
          />
        </div>
      </main>
    );
  };

  return (
    <div className="flex flex-col bg-bg h-full">
      <div className="max-w-7xl mx-auto px-6 py-4 w-full flex flex-col flex-1 min-h-0">
      <nav className="mb-4 pb-2 border-b border-border">
        <DateBadge selectedDate={selectedDate} onSelectDate={handleDateChange} />
      </nav>

      {renderContent()}
      </div>
    </div>
  );
}
