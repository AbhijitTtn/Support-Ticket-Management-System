import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createTicket } from '../api/tickets';
import { fieldErrorsToMap, getErrorMessage, isApiError } from '../api/errors';
import { ErrorBanner } from '../components/ErrorBanner';
import { TicketForm, type TicketFormValues } from '../components/TicketForm';

export function CreateTicketPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const handleSubmit = async (values: TicketFormValues) => {
    setLoading(true);
    setError(null);
    setFieldErrors({});

    try {
      const created = await createTicket({
        title: values.title,
        description: values.description,
        priority: values.priority,
        assignee: values.assignee.trim() || null,
      });
      navigate(`/tickets/${created.id}`);
    } catch (err) {
      if (isApiError(err) && err.fieldErrors.length > 0) {
        setFieldErrors(fieldErrorsToMap(err.fieldErrors));
      } else {
        setError(getErrorMessage(err, 'Failed to create ticket. Please try again.'));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="page-section">
      <div className="page-header">
        <h1>Create Ticket</h1>
        <p className="page-subtitle">Open a new support request.</p>
      </div>

      {error && <ErrorBanner message={error} onDismiss={() => setError(null)} />}

      <TicketForm
        initialValues={{
          title: '',
          description: '',
          priority: 'MEDIUM',
          assignee: '',
        }}
        submitLabel="Create Ticket"
        loading={loading}
        fieldErrors={fieldErrors}
        onSubmit={handleSubmit}
        onCancel={() => navigate('/')}
      />
    </section>
  );
}
