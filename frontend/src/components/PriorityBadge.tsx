import type { TicketPriority } from '../types/ticket';

const LABELS: Record<TicketPriority, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  CRITICAL: 'Critical',
};

type PriorityBadgeProps = {
  priority: TicketPriority;
};

export function PriorityBadge({ priority }: PriorityBadgeProps) {
  return (
    <span className={`badge badge-priority badge-priority-${priority.toLowerCase()}`}>
      {LABELS[priority]}
    </span>
  );
}
