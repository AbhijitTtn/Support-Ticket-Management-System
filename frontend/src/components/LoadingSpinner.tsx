type LoadingSpinnerProps = {
  label?: string;
  inline?: boolean;
};

export function LoadingSpinner({ label = 'Loading…', inline = false }: LoadingSpinnerProps) {
  return (
    <div className={inline ? 'loading-inline' : 'loading-block'} role="status" aria-live="polite">
      <span className="spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}
