import { ClientError } from '$lib/errors/ClientError';

export function handleHttpError(response: Response): Promise<Response | void> {
  return new Promise<Response>((resolve, reject) => {
    if (!response.ok) {
      // backend reported error

      // check status code and throw appropriate error
      if (response.status === 401) {
        // KeycloakService.getKeycloakInstance().clearToken();
        // reject(
        //   new AuthenticationError("You are not authenticated to access the resource, please login.", response.status)
        // );
      } else if (response.status === 403) {
        // reject(new AuthorizationError("You don't have permissions to access the resource!", response.status));
      } else if (response.status >= 400 && response.status <= 451) {
        handleClientError(response, reject);
      } // else if (response.status === 501) {
      //   reject(new ServerError("Server error, not implemented", response.status));
      // } else if (response.status >= 500 && response.status <= 599) {
      //   reject(new ServerError("Server error occurred", response.status));
      // } else {
      //   reject(new HttpResponseError("Unhandled HTTP response error", response.status));
      // }
    } else {
      // successful request
      resolve(response);
    }
  });
}

type ErrorResponse = {
  error: string;
  message: string;
  status: number;
};

function handleClientError(response: Response, reject: (e: Error) => void): void {
  response
    .json()
    .then((e: ErrorResponse) => {
      reject(new ClientError(e.status, e.message, e.error));
    })
    .catch(e => reject(e));
}
