interface SeriesDeleteModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSkip: () => void;
  onStop: () => void;
}

export default function SeriesDeleteModal({ isOpen, onClose, onSkip, onStop }: SeriesDeleteModalProps) {
  if (!isOpen) return null;

  return (
    <div style={styles.overlay} onClick={onClose}>
      <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
        <h3 style={styles.title}>반복 Task 삭제</h3>
        <p style={styles.description}>이 Task는 반복 시리즈입니다.</p>
        <div style={styles.buttons}>
          <button style={styles.skipButton} onClick={onSkip}>
            이 날짜만 삭제
          </button>
          <button style={styles.stopButton} onClick={onStop}>
            이후 반복 중단
          </button>
          <button style={styles.cancelButton} onClick={onClose}>
            취소
          </button>
        </div>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  overlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  modal: {
    backgroundColor: '#fff',
    borderRadius: '12px',
    padding: '24px',
    width: '320px',
    boxShadow: '0 20px 60px rgba(0, 0, 0, 0.15)',
  },
  title: {
    margin: '0 0 8px 0',
    fontSize: '16px',
    fontWeight: '600',
    color: '#111827',
  },
  description: {
    margin: '0 0 20px 0',
    fontSize: '14px',
    color: '#6b7280',
  },
  buttons: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  skipButton: {
    padding: '10px 16px',
    backgroundColor: '#2563eb',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '14px',
    fontWeight: '500',
    cursor: 'pointer',
  },
  stopButton: {
    padding: '10px 16px',
    backgroundColor: '#ef4444',
    color: '#fff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '14px',
    fontWeight: '500',
    cursor: 'pointer',
  },
  cancelButton: {
    padding: '10px 16px',
    backgroundColor: '#f3f4f6',
    color: '#374151',
    border: 'none',
    borderRadius: '8px',
    fontSize: '14px',
    fontWeight: '500',
    cursor: 'pointer',
  },
};
