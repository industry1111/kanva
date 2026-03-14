interface CalendarModalProps {
  isOpen: boolean;
  currentDate: string;
  onClose: () => void;
  onSelectDate: (date: string) => void;
}

export default function CalendarModal({
  isOpen,
  currentDate,
  onClose,
  onSelectDate,
}: CalendarModalProps) {
  if (!isOpen) return null;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onSelectDate(e.target.value);
    onClose();
  };

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div
      className="fixed inset-0 bg-black/30 flex items-center justify-center z-50"
      onClick={handleBackdropClick}
    >
      <div className="bg-white rounded-xl border border-border p-4 shadow-lg min-w-[260px] text-center">
        <h3 className="text-[13px] font-semibold text-text mt-0 mb-3">날짜 선택</h3>
        <input
          type="date"
          value={currentDate}
          onChange={handleChange}
          className="w-full py-1.5 px-2.5 border border-border rounded-lg text-[13px] text-text outline-none mb-3"
        />
        <button
          onClick={onClose}
          className="py-1.5 px-5 bg-text-secondary text-white border-none rounded-md cursor-pointer text-[13px]"
        >
          닫기
        </button>
      </div>
    </div>
  );
}
