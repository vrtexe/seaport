import { handleHttpError } from '$lib/errors/error';
import type { Middleware, ResponseContext } from '$lib/generated';

export class ErrorMiddleware implements Middleware {
  post(context: ResponseContext): Promise<Response | void> {
    return handleHttpError(context.response);
  }
}
