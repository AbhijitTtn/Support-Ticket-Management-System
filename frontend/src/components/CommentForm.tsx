import { useState, type FormEvent } from 'react';
import { FieldError } from './FieldError';

type CommentFormValues = {
  author: string;
  body: string;
};

type CommentFormProps = {
  loading?: boolean;
  fieldErrors?: Record<string, string>;
  onSubmit: (values: CommentFormValues) => void | Promise<void>;
};

export function CommentForm({ loading = false, fieldErrors = {}, onSubmit }: CommentFormProps) {
  const [values, setValues] = useState<CommentFormValues>({ author: '', body: '' });

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    try {
      await onSubmit(values);
      setValues({ author: '', body: '' });
    } catch {
      // Preserve entered values when submit fails (validation or API error).
    }
  };

  return (
    <form className="comment-form" onSubmit={handleSubmit}>
      <div className="form-field">
        <label htmlFor="comment-author">Author</label>
        <input
          id="comment-author"
          type="text"
          value={values.author}
          onChange={(event) => setValues((current) => ({ ...current, author: event.target.value }))}
          maxLength={120}
          required
          disabled={loading}
        />
        <FieldError message={fieldErrors.author} />
      </div>
      <div className="form-field">
        <label htmlFor="comment-body">Comment</label>
        <textarea
          id="comment-body"
          value={values.body}
          onChange={(event) => setValues((current) => ({ ...current, body: event.target.value }))}
          maxLength={2000}
          rows={3}
          required
          disabled={loading}
        />
        <FieldError message={fieldErrors.body} />
      </div>
      <button type="submit" className="button button-primary" disabled={loading}>
        {loading ? 'Adding…' : 'Add Comment'}
      </button>
    </form>
  );
}
