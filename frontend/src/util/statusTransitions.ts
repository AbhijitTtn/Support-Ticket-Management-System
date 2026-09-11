import type { TicketStatus } from '../types/ticket';

export type StatusAction = {
  label: string;
  target: TicketStatus;
  destructive?: boolean;
};

const STATUS_ACTIONS: Record<TicketStatus, StatusAction[]> = {
  OPEN: [
    { label: 'Start Progress', target: 'IN_PROGRESS' },
    { label: 'Cancel Ticket', target: 'CANCELLED', destructive: true },
  ],
  IN_PROGRESS: [
    { label: 'Mark Resolved', target: 'RESOLVED' },
    { label: 'Cancel Ticket', target: 'CANCELLED', destructive: true },
  ],
  RESOLVED: [{ label: 'Close Ticket', target: 'CLOSED' }],
  CLOSED: [],
  CANCELLED: [],
};

export function getLegalStatusActions(status: TicketStatus): StatusAction[] {
  return STATUS_ACTIONS[status];
}

export function getTerminalStatusMessage(status: TicketStatus): string | null {
  if (status === 'CLOSED') {
    return 'This ticket is closed.';
  }
  if (status === 'CANCELLED') {
    return 'This ticket is cancelled.';
  }
  return null;
}
