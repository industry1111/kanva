interface SeriesDeleteModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSkip: () => void;
  onStop: () => void;
}

export default function SeriesDeleteModal({ isOpen, onClose, onSkip, onStop }: SeriesDeleteModalProps) {
  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-xl p-4 w-[300px] shadow-xl mx-4"
        onClick={(e) => e.stopPropagation()}
      >
        <h3 className="m-0 mb-1.5 text-[13px] font-semibold text-text">반복 Task 삭제</h3>
        <p className="m-0 mb-3 text-[13px] text-text-secondary">이 Task는 반복 시리즈입니다.</p>
        <div className="flex flex-col gap-1.5">
          <button
            className="py-2 px-3 bg-primary text-white border-none rounded-lg text-[13px] font-medium cursor-pointer transition-colors hover:bg-primary-hover"
            onClick={onSkip}
          >
            이 날짜만 삭제
          </button>
          <button
            className="py-2 px-3 bg-danger text-white border-none rounded-lg text-[13px] font-medium cursor-pointer transition-colors hover:opacity-90"
            onClick={onStop}
          >
            이후 반복 중단
          </button>
          <button
            className="py-2 px-3 bg-bg text-text border-none rounded-lg text-[13px] font-medium cursor-pointer transition-colors hover:bg-border/30"
            onClick={onClose}
          >
            취소
          </button>
        </div>
      </div>
    </div>
  );
}
