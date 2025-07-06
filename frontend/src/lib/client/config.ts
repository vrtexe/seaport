import { authenticationMiddleware } from '$lib/client/middlewares/authMiddleware';
import { BASE_URL } from '$lib/config';
import { ErrorMiddleware } from '$lib/client/middlewares/errorMiddleware';
import { Configuration } from '$lib/generated';

export const configuration = new Configuration({
  basePath: BASE_URL,
  middleware: [new ErrorMiddleware(), authenticationMiddleware()]
});
