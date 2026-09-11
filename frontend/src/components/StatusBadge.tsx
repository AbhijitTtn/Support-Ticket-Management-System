import type { TicketStatus } from '../types/ticket';

const LABELS: Record<TicketStatus, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In Progress',
  RESOLVED: 'Resolved',
  CLOSED: 'Closed',
  CANCELLED: 'Cancelled',
};

type StatusBadgeProps = {
  status: TicketStatus;
};

export function StatusBadge({ status }: StatusBadgeProps) {
  const cssStatus = status.toLowerCase().replace('_', '-');

  return (
    <span className={`badge badge-status badge-status-${cssStatus}`}>
      {LABELS[status]}
    </span>
  );
}
