import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listTickets } from '../api/tickets';
import { getErrorMessage } from '../api/errors';
import type { TicketStatus, TicketSummary } from '../types/ticket';
import { useDebouncedValue } from '../util/useDebouncedValue';
import { formatRelativeTime, truncateText } from '../util/formatDate';
import { ErrorBanner } from './ErrorBanner';
import { LoadingSpinner } from './LoadingSpinner';
import { PriorityBadge } from './PriorityBadge';
import { StatusBadge } from './StatusBadge';

const STATUS_OPTIONS: { label: string; value: '' | TicketStatus }[] = [
  { label: 'All', value: '' },
  { label: 'Open', value: 'OPEN' },
  { label: 'In Progress', value: 'IN_PROGRESS' },
  { label: 'Resolved', value: 'RESOLVED' },
  { label: 'Closed', value: 'CLOSED' },
  { label: 'Cancelled', value: 'CANCELLED' },
];

export function TicketList() {
  const [searchInput, setSearchInput] = useState('');
  const [statusFilter, setStatusFilter] = useState<'' | TicketStatus>('');
  const debouncedSearch = useDebouncedValue(searchInput, 300);

  const [tickets, setTickets] = useState<TicketSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadTickets = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const keyword = debouncedSearch.trim();
      const data = await listTickets({
        q: keyword || undefined,
        status: statusFilter || undefined,
      });
      setTickets(data);
    } catch (err) {
      setTickets([]);
      setError(getErrorMessage(err, 'Unable to load tickets. Please try again.'));
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch, statusFilter]);

  useEffect(() => {
    void loadTickets();
  }, [loadTickets]);

  const activeFilterLabel = STATUS_OPTIONS.find((option) => option.value === statusFilter)?.label;
  const showFilterHint = statusFilter !== '' || debouncedSearch.trim() !== '';

  return (
    <section className="page-section">
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p className="page-subtitle">Browse, search, and filter support tickets.</p>
        </div>
      </div>

      <div className="toolbar card">
        <div className="toolbar-field">
          <label htmlFor="search">Search</label>
          <input
            id="search"
            type="search"
            placeholder="Search title or description"
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
            disabled={loading && tickets.length === 0}
          />
        </div>
        <div className="toolbar-field">
          <label htmlFor="status-filter">Status</label>
          <select
            id="status-filter"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as '' | TicketStatus)}
            disabled={loading && tickets.length === 0}
          >
            {STATUS_OPTIONS.map((option) => (
              <option key={option.label} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>
      </div>

      {showFilterHint && !error && (
        <p className="filter-hint">
          Showing: {statusFilter ? `${activeFilterLabel} tickets` : 'all tickets'}
          {debouncedSearch.trim() ? ` matching "${debouncedSearch.trim()}"` : ''}
        </p>
      )}

      {error && (
        <ErrorBanner message={error} onRetry={() => void loadTickets()} onDismiss={() => setError(null)} />
      )}

      <div className="card table-card">
        {loading ? (
          <LoadingSpinner label="Loading tickets…" />
        ) : tickets.length === 0 ? (
          <div className="empty-state">
            <p>No tickets found.</p>
            <Link to="/tickets/new" className="button button-primary">Create a ticket</Link>
          </div>
        ) : (
          <table className="ticket-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Assignee</th>
                <th>Updated</th>
              </tr>
            </thead>
            <tbody>
              {tickets.map((ticket) => (
                <tr key={ticket.id}>
                  <td>
                    <Link to={`/tickets/${ticket.id}`} className="ticket-link">
                      {truncateText(ticket.title, 60)}
                    </Link>
                  </td>
                  <td><PriorityBadge priority={ticket.priority} /></td>
                  <td><StatusBadge status={ticket.status} /></td>
                  <td>{ticket.assignee ?? '—'}</td>
                  <td>{formatRelativeTime(ticket.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}
