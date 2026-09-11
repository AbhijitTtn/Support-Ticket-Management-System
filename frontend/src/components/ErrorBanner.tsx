type ErrorBannerProps = {
  message: string;
  onDismiss?: () => void;
  onRetry?: () => void;
};

export function ErrorBanner({ message, onDismiss, onRetry }: ErrorBannerProps) {
  return (
    <div className="error-banner" role="alert">
      <span>{message}</span>
      <div className="error-banner-actions">
        {onRetry && (
          <button type="button" className="button button-secondary button-small" onClick={onRetry}>
            Retry
          </button>
        )}
        {onDismiss && (
          <button type="button" className="button button-ghost button-small" onClick={onDismiss}>
            Dismiss
          </button>
        )}
      </div>
    </div>
  );
}
