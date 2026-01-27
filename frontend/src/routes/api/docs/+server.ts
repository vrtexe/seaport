import { redirect } from '@sveltejs/kit';
import type { RequestHandler } from './$types';

export const GET: RequestHandler = () => {
  // Redirect API callers to the human-friendly docs page
  throw redirect(307, '/docs');
};
