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
      <button
        onClick={() => setIsEditing(true)}
        className="w-full py-1 px-1.5 bg-transparent border border-dashed border-border rounded-md text-text-secondary cursor-pointer text-[13px]"
      >
        + 할 일 추가
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
      className="w-full py-1 px-1.5 border border-primary rounded-md text-[13px] outline-none box-border"
    />
  );
}
