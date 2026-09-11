import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  addComment,
  getTicket,
  transitionStatus,
  updateTicket,
} from '../api/tickets';
import { fieldErrorsToMap, getErrorMessage, isApiError } from '../api/errors';
import type { TicketDetail as TicketDetailType, TicketStatus } from '../types/ticket';
import { formatDateTime } from '../util/formatDate';
import { getLegalStatusActions, getTerminalStatusMessage } from '../util/statusTransitions';
import { CommentForm } from './CommentForm';
import { ErrorBanner } from './ErrorBanner';
import { LoadingSpinner } from './LoadingSpinner';
import { PriorityBadge } from './PriorityBadge';
import { StatusBadge } from './StatusBadge';
import { TicketForm, type TicketFormValues } from './TicketForm';

type TicketDetailProps = {
  ticketId: string;
};

export function TicketDetail({ ticketId }: TicketDetailProps) {
  const [ticket, setTicket] = useState<TicketDetailType | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [editing, setEditing] = useState(false);
  const [savingEdit, setSavingEdit] = useState(false);
  const [editFieldErrors, setEditFieldErrors] = useState<Record<string, string>>({});

  const [transitioningTarget, setTransitioningTarget] = useState<TicketStatus | null>(null);

  const [commentLoading, setCommentLoading] = useState(false);
  const [commentFieldErrors, setCommentFieldErrors] = useState<Record<string, string>>({});

  const loadTicket = useCallback(async () => {
    setLoading(true);
    setError(null);
    setNotFound(false);

    try {
      const data = await getTicket(ticketId);
      setTicket(data);
    } catch (err) {
      setTicket(null);
      if (isApiError(err) && err.status === 404) {
        setNotFound(true);
      } else {
        setError(getErrorMessage(err, 'Unable to load ticket. Please try again.'));
      }
    } finally {
      setLoading(false);
    }
  }, [ticketId]);

  useEffect(() => {
    void loadTicket();
  }, [loadTicket]);

  const handleEditSubmit = async (values: TicketFormValues) => {
    if (!ticket) {
      return;
    }

    setSavingEdit(true);
    setEditFieldErrors({});
    setError(null);

    try {
      const updated = await updateTicket(ticket.id, {
        title: values.title,
        description: values.description,
        priority: values.priority,
        assignee: values.assignee.trim() || null,
      });
      setTicket(updated);
      setEditing(false);
    } catch (err) {
      if (isApiError(err) && err.fieldErrors.length > 0) {
        setEditFieldErrors(fieldErrorsToMap(err.fieldErrors));
      } else {
        setError(getErrorMessage(err, 'Failed to update ticket. Please try again.'));
      }
    } finally {
      setSavingEdit(false);
    }
  };

  const handleStatusTransition = async (target: TicketStatus, destructive: boolean) => {
    if (!ticket) {
      return;
    }

    if (destructive) {
      const confirmed = window.confirm('Cancel this ticket?');
      if (!confirmed) {
        return;
      }
    }

    setTransitioningTarget(target);
    setError(null);

    try {
      const updated = await transitionStatus(ticket.id, { status: target });
      setTicket(updated);
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to update ticket status.'));
    } finally {
      setTransitioningTarget(null);
    }
  };

  const handleAddComment = async (values: { author: string; body: string }) => {
    if (!ticket) {
      return;
    }

    setCommentLoading(true);
    setCommentFieldErrors({});
    setError(null);

    try {
      const comment = await addComment(ticket.id, values);
      setTicket((current) =>
        current ? { ...current, comments: [...current.comments, comment] } : current,
      );
    } catch (err) {
      if (isApiError(err) && err.fieldErrors.length > 0) {
        setCommentFieldErrors(fieldErrorsToMap(err.fieldErrors));
      } else if (isApiError(err) && err.status === 404) {
        setNotFound(true);
      } else {
        setError(getErrorMessage(err, 'Failed to add comment. Please try again.'));
      }
      throw err;
    } finally {
      setCommentLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner label="Loading ticket…" />;
  }

  if (notFound) {
    return (
      <section className="page-section">
        <div className="empty-state card">
          <h1>Ticket not found</h1>
          <p>The ticket you are looking for does not exist.</p>
          <Link to="/" className="button button-primary">Back to dashboard</Link>
        </div>
      </section>
    );
  }

  if (!ticket) {
    return (
      <section className="page-section">
        {error && (
          <ErrorBanner message={error} onRetry={() => void loadTicket()} onDismiss={() => setError(null)} />
        )}
      </section>
    );
  }

  const statusActions = getLegalStatusActions(ticket.status);
  const terminalMessage = getTerminalStatusMessage(ticket.status);

  return (
    <section className="page-section">
      <Link to="/" className="back-link">← Back to list</Link>

      {error && (
        <ErrorBanner message={error} onDismiss={() => setError(null)} />
      )}

      <div className="card ticket-detail-header">
        <div className="ticket-detail-title-row">
          <h1>{ticket.title}</h1>
          <div className="badge-row">
            <PriorityBadge priority={ticket.priority} />
            <StatusBadge status={ticket.status} />
          </div>
        </div>
        <dl className="meta-grid">
          <div>
            <dt>Assignee</dt>
            <dd>{ticket.assignee ?? '—'}</dd>
          </div>
          <div>
            <dt>Created</dt>
            <dd>{formatDateTime(ticket.createdAt)}</dd>
          </div>
          <div>
            <dt>Updated</dt>
            <dd>{formatDateTime(ticket.updatedAt)}</dd>
          </div>
        </dl>
      </div>

      {!editing ? (
        <>
          <div className="card">
            <h2>Description</h2>
            <p className="ticket-description">{ticket.description}</p>
          </div>

          <div className="card">
            <h2>Status actions</h2>
            {statusActions.length > 0 ? (
              <div className="status-actions">
                {statusActions.map((action) => (
                  <button
                    key={action.target}
                    type="button"
                    className={action.destructive ? 'button button-danger' : 'button button-secondary'}
                    disabled={transitioningTarget !== null}
                    onClick={() => void handleStatusTransition(action.target, Boolean(action.destructive))}
                  >
                    {transitioningTarget === action.target ? 'Updating…' : action.label}
                  </button>
                ))}
              </div>
            ) : (
              <p className="muted-text">{terminalMessage}</p>
            )}
          </div>

          <div className="card">
            <button
              type="button"
              className="button button-secondary"
              onClick={() => {
                setEditing(true);
                setEditFieldErrors({});
                setError(null);
              }}
            >
              Edit Ticket
            </button>
          </div>
        </>
      ) : (
        <div>
          <h2>Edit ticket</h2>
          <TicketForm
            initialValues={{
              title: ticket.title,
              description: ticket.description,
              priority: ticket.priority,
              assignee: ticket.assignee ?? '',
            }}
            submitLabel="Save Changes"
            loading={savingEdit}
            fieldErrors={editFieldErrors}
            onSubmit={handleEditSubmit}
            onCancel={() => setEditing(false)}
          />
        </div>
      )}

      <div className="card">
        <h2>Comments ({ticket.comments.length})</h2>
        {ticket.comments.length === 0 ? (
          <p className="muted-text">No comments yet.</p>
        ) : (
          <ul className="comment-list">
            {ticket.comments.map((comment) => (
              <li key={comment.id} className="comment-item">
                <div className="comment-meta">
                  <strong>{comment.author}</strong>
                  <span>{formatDateTime(comment.createdAt)}</span>
                </div>
                <p>{comment.body}</p>
              </li>
            ))}
          </ul>
        )}

        <CommentForm
          loading={commentLoading}
          fieldErrors={commentFieldErrors}
          onSubmit={handleAddComment}
        />
      </div>
    </section>
  );
}
