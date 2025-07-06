import { WEB_SOCKET_BASE_URL } from '$lib/config';

export type BuildStatus = (typeof BuildStatus)[keyof typeof BuildStatus];
export const BuildStatus = Object.freeze({
  Initialized: 'INITIALIZED',
  Started: 'STARTED',
  Completed: 'COMPLETED',
  Failed: 'FAILED'
} as const);

export type StatusMessageResponse = {
  status: BuildStatus;
};

export type LogMessageResponse = {
  status: BuildStatus;
  data: string;
};

export type BuildStatusMessage = {
  uid: string;
};

export function connectBuildStatusWebSocket(
  request: BuildStatusMessage,
  handle: (message: StatusMessageResponse) => unknown
) {
  const ws = new WebSocket(`${WEB_SOCKET_BASE_URL}/build/status`);
  ws.onmessage = (message: MessageEvent<string>) => {
    const messageData = JSON.parse(message.data) as StatusMessageResponse;
    handle(messageData);
  };

  ws.onopen = () => {
    ws.send(JSON.stringify(request));
  };

  return ws;
}

export function connectBuildLogWebSocket(
  request: BuildStatusMessage,
  handle: (message: LogMessageResponse) => unknown
) {
  const ws = new WebSocket(`${WEB_SOCKET_BASE_URL}/build/logs`);
  ws.onmessage = (message: MessageEvent<string>) => {
    const messageData = JSON.parse(message.data) as LogMessageResponse;
    handle(messageData);
  };

  ws.onopen = () => {
    ws.send(JSON.stringify(request));
  };

  return ws;
}
