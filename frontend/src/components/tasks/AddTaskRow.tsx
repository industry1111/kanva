import { useState } from 'react';

interface AddTaskRowProps {
  onAdd: (title: string) => void;
}

export default function AddTaskRow({ onAdd }: AddTaskRowProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [value, setValue] = useState('');

  const handleSubmit = () => {
    const trimmed = value.trim();
    if (trimmed) {
      onAdd(trimmed);
    }
    setValue('');
    setIsEditing(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSubmit();
    } else if (e.key === 'Escape') {
      setValue('');
      setIsEditing(false);
    }
  };

  if (!isEditing) {
    return (
      <button onClick={() => setIsEditing(true)} style={styles.addButton}>
        + Add Task
      </button>
    );
  }

  return (
    <input
      type="text"
      value={value}
      onChange={(e) => setValue(e.target.value)}
      onKeyDown={handleKeyDown}
      onBlur={handleSubmit}
      placeholder="새 할 일 입력 (Enter로 추가, Esc로 취소)"
      autoFocus
      style={styles.input}
    />
  );
}

const styles: Record<string, React.CSSProperties> = {
  addButton: {
    width: '100%',
    padding: '12px',
    backgroundColor: 'transparent',
    border: '2px dashed #d1d5db',
    borderRadius: '8px',
    color: '#6b7280',
    cursor: 'pointer',
    fontSize: '14px',
  },
  input: {
    width: '100%',
    padding: '12px',
    border: '2px solid #3b82f6',
    borderRadius: '8px',
    fontSize: '14px',
    boxSizing: 'border-box',
  },
};
