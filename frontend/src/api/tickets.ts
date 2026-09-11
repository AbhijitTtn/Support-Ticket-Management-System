import { apiRequest } from './client';
import type {
  Comment,
  CreateCommentRequest,
  CreateTicketRequest,
  TicketDetail,
  TicketStatus,
  TicketSummary,
  TransitionStatusRequest,
  UpdateTicketRequest,
} from '../types/ticket';

export type ListTicketsParams = {
  q?: string;
  status?: TicketStatus;
};

function buildQuery(params: ListTicketsParams): string {
  const searchParams = new URLSearchParams();

  if (params.q) {
    searchParams.set('q', params.q);
  }

  if (params.status) {
    searchParams.set('status', params.status);
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function listTickets(params: ListTicketsParams = {}): Promise<TicketSummary[]> {
  return apiRequest<TicketSummary[]>(`/tickets${buildQuery(params)}`);
}

export function getTicket(id: string): Promise<TicketDetail> {
  return apiRequest<TicketDetail>(`/tickets/${id}`);
}

export function createTicket(request: CreateTicketRequest): Promise<TicketDetail> {
  return apiRequest<TicketDetail>('/tickets', { method: 'POST', body: request });
}

export function updateTicket(id: string, request: UpdateTicketRequest): Promise<TicketDetail> {
  return apiRequest<TicketDetail>(`/tickets/${id}`, { method: 'PATCH', body: request });
}

export function transitionStatus(
  id: string,
  request: TransitionStatusRequest,
): Promise<TicketDetail> {
  return apiRequest<TicketDetail>(`/tickets/${id}/status`, { method: 'PATCH', body: request });
}

export function addComment(id: string, request: CreateCommentRequest): Promise<Comment> {
  return apiRequest<Comment>(`/tickets/${id}/comments`, { method: 'POST', body: request });
}
