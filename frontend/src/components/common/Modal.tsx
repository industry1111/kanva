import type { ReactNode } from 'react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  width?: string;
  children: ReactNode;
  footer?: ReactNode;
}

export default function Modal({
  isOpen,
  onClose,
  title,
  width = 'w-[380px]',
  children,
  footer,
}: ModalProps) {
  if (!isOpen) return null;

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) onClose();
  };

  return (
    <div
      className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      onClick={handleBackdropClick}
    >
      <div className={`bg-white rounded-xl p-4 ${width} max-w-[90vw] max-h-[90vh] flex flex-col shadow-xl mx-4`}>
        <div className="flex justify-between items-center mb-3">
          <h2 className="m-0 text-[15px] font-semibold text-text">{title}</h2>
          <button
            onClick={onClose}
            className="w-7 h-7 border-none rounded-md bg-transparent cursor-pointer text-xl text-text-secondary flex items-center justify-center transition-colors hover:bg-bg"
            aria-label="Close"
          >
            ×
          </button>
        </div>

        <div className="flex-1 overflow-y-auto">{children}</div>

        {footer && (
          <div className="pt-3 mt-3 border-t border-border">{footer}</div>
        )}
      </div>
    </div>
  );
}
