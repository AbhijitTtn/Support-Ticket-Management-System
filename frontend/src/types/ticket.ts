export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type TicketStatus =
  | 'OPEN'
  | 'IN_PROGRESS'
  | 'RESOLVED'
  | 'CLOSED'
  | 'CANCELLED';

export interface TicketSummary {
  id: string;
  title: string;
  priority: TicketPriority;
  status: TicketStatus;
  assignee: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: string;
  ticketId: string;
  author: string;
  body: string;
  createdAt: string;
}

export interface TicketDetail {
  id: string;
  title: string;
  description: string;
  priority: TicketPriority;
  status: TicketStatus;
  assignee: string | null;
  createdAt: string;
  updatedAt: string;
  comments: Comment[];
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
  assignee?: string | null;
}

export interface UpdateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
  assignee?: string | null;
}

export interface TransitionStatusRequest {
  status: TicketStatus;
}

export interface CreateCommentRequest {
  author: string;
  body: string;
}

export interface FieldError {
  field: string;
  message: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors: FieldError[];
}
