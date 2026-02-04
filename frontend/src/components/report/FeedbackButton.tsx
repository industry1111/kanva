import { useState } from 'react';
import type { ReportFeedback } from '../../types/report';

interface FeedbackButtonProps {
  currentFeedback?: ReportFeedback;
  onSubmitFeedback: (feedback: ReportFeedback) => Promise<void>;
  disabled?: boolean;
}

export default function FeedbackButton({
  currentFeedback,
  onSubmitFeedback,
  disabled = false,
}: FeedbackButtonProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [selectedFeedback, setSelectedFeedback] = useState<ReportFeedback | undefined>(
    currentFeedback
  );

  const handleFeedback = async (feedback: ReportFeedback) => {
    if (isSubmitting || disabled) return;

    setIsSubmitting(true);
    try {
      await onSubmitFeedback(feedback);
      setSelectedFeedback(feedback);
    } catch (error) {
      console.error('Failed to submit feedback:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const feedbackOptions: { type: ReportFeedback; label: string; icon: string }[] = [
    { type: 'HELPFUL', label: '도움됨', icon: '👍' },
    { type: 'NOT_HELPFUL', label: '도움 안됨', icon: '👎' },
    { type: 'NEUTRAL', label: '보통', icon: '😐' },
  ];

  return (
    <div className="feedback-section">
      <span className="feedback-label">이 리포트가 도움이 되었나요?</span>
      <div className="feedback-buttons">
        {feedbackOptions.map(({ type, label, icon }) => (
          <button
            key={type}
            className={`feedback-btn ${selectedFeedback === type ? 'selected' : ''}`}
            onClick={() => handleFeedback(type)}
            disabled={isSubmitting || disabled}
            title={label}
          >
            {icon}
          </button>
        ))}
      </div>
      {selectedFeedback && (
        <span className="feedback-thanks">피드백 감사합니다!</span>
      )}
    </div>
  );
}
