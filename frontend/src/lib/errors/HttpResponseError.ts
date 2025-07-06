export class HttpResponseError extends Error {
  status: number;
  error: string;

  constructor(status: number, message: string, error: string) {
    super(message);
    this.status = status;
    this.error = error;
  }
}
