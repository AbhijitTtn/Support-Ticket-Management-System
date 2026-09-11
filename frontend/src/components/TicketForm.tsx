import { useState, type FormEvent } from 'react';
import type { TicketPriority } from '../types/ticket';
import { FieldError } from './FieldError';

export type TicketFormValues = {
  title: string;
  description: string;
  priority: TicketPriority;
  assignee: string;
};

type TicketFormProps = {
  initialValues: TicketFormValues;
  submitLabel: string;
  loading?: boolean;
  fieldErrors?: Record<string, string>;
  onSubmit: (values: TicketFormValues) => void | Promise<void>;
  onCancel?: () => void;
};

const PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export function TicketForm({
  initialValues,
  submitLabel,
  loading = false,
  fieldErrors = {},
  onSubmit,
  onCancel,
}: TicketFormProps) {
  const [values, setValues] = useState<TicketFormValues>(initialValues);

  const updateField = <K extends keyof TicketFormValues>(field: K, value: TicketFormValues[K]) => {
    setValues((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    await onSubmit(values);
  };

  return (
    <form className="ticket-form card" onSubmit={handleSubmit}>
      <div className="form-field">
        <label htmlFor="title">Title</label>
        <input
          id="title"
          type="text"
          value={values.title}
          onChange={(event) => updateField('title', event.target.value)}
          maxLength={120}
          required
          disabled={loading}
        />
        <FieldError message={fieldErrors.title} />
      </div>

      <div className="form-field">
        <label htmlFor="description">Description</label>
        <textarea
          id="description"
          value={values.description}
          onChange={(event) => updateField('description', event.target.value)}
          maxLength={5000}
          rows={6}
          required
          disabled={loading}
        />
        <FieldError message={fieldErrors.description} />
      </div>

      <div className="form-field">
        <label htmlFor="priority">Priority</label>
        <select
          id="priority"
          value={values.priority}
          onChange={(event) => updateField('priority', event.target.value as TicketPriority)}
          disabled={loading}
        >
          {PRIORITIES.map((priority) => (
            <option key={priority} value={priority}>{priority}</option>
          ))}
        </select>
        <FieldError message={fieldErrors.priority} />
      </div>

      <div className="form-field">
        <label htmlFor="assignee">Assignee (optional)</label>
        <input
          id="assignee"
          type="text"
          value={values.assignee}
          onChange={(event) => updateField('assignee', event.target.value)}
          maxLength={120}
          disabled={loading}
        />
        <FieldError message={fieldErrors.assignee} />
      </div>

      <div className="form-actions">
        {onCancel && (
          <button type="button" className="button button-secondary" onClick={onCancel} disabled={loading}>
            Cancel
          </button>
        )}
        <button type="submit" className="button button-primary" disabled={loading}>
          {loading ? 'Saving…' : submitLabel}
        </button>
      </div>
    </form>
  );
}
