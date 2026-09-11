import { ApiError } from './errors';
import type { ErrorResponse } from '../types/ticket';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

type RequestOptions = {
  method?: string;
  body?: unknown;
};

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body } = options;

  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError({
      timestamp: new Date().toISOString(),
      status: 0,
      error: 'Network Error',
      message: 'Unable to reach the server. Check your connection.',
      path,
      fieldErrors: [],
    });
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get('content-type');
  const hasJson = contentType?.includes('application/json');
  const payload = hasJson ? await response.json() : null;

  if (!response.ok) {
    if (payload && typeof payload === 'object' && 'message' in payload) {
      throw new ApiError(payload as ErrorResponse);
    }

    throw new ApiError({
      timestamp: new Date().toISOString(),
      status: response.status,
      error: response.statusText || 'Error',
      message: 'Something went wrong. Please try again.',
      path,
      fieldErrors: [],
    });
  }

  return payload as T;
}
