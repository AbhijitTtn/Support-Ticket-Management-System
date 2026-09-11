import type { ErrorResponse, FieldError } from '../types/ticket';

export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors: FieldError[];
  readonly errorResponse: ErrorResponse;

  constructor(errorResponse: ErrorResponse) {
    super(errorResponse.message);
    this.name = 'ApiError';
    this.status = errorResponse.status;
    this.fieldErrors = errorResponse.fieldErrors ?? [];
    this.errorResponse = errorResponse;
  }

  getFieldError(field: string): string | undefined {
    return this.fieldErrors.find((entry) => entry.field === field)?.message;
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError;
}

export function getErrorMessage(error: unknown, fallback: string): string {
  if (isApiError(error)) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

export function fieldErrorsToMap(fieldErrors: FieldError[]): Record<string, string> {
  return fieldErrors.reduce<Record<string, string>>((map, entry) => {
    map[entry.field] = entry.message;
    return map;
  }, {});
}
