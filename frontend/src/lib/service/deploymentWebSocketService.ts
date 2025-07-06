import { WEB_SOCKET_BASE_URL } from '$lib/config';

export type DeploymentStatus = (typeof DeploymentStatus)[keyof typeof DeploymentStatus];
export const DeploymentStatus = Object.freeze({
  Initial: 'INITIAL',
  Started: 'STARTED',
  Stopped: 'STOPPED',
  Failed: 'FAILED'
} as const);

export type StatusMessageResponse = {
  status: DeploymentStatus;
};

export type LogMessageResponse = {
  data: string;
};

export type DeploymentRequestMessage = {
  uid: string;
};

export function connectDeploymentStatusWebSocket(
  request: DeploymentRequestMessage,
  handle: (message: StatusMessageResponse) => unknown
) {
  const ws = new WebSocket(`${WEB_SOCKET_BASE_URL}/deployment/status`);
  ws.onmessage = (message: MessageEvent<string>) => {
    const messageData = JSON.parse(message.data) as StatusMessageResponse;
    handle(messageData);
  };

  ws.onopen = () => {
    ws.send(JSON.stringify(request));
  };

  return ws;
}

export function connectDeploymentLogWebSocket(
  request: DeploymentRequestMessage,
  handle: (message: LogMessageResponse) => unknown
) {
  const ws = new WebSocket(`${WEB_SOCKET_BASE_URL}/deployment/logs`);
  ws.onmessage = (message: MessageEvent<string>) => {
    const messageData = JSON.parse(message.data) as LogMessageResponse;
    handle(messageData);
  };

  ws.onopen = () => {
    ws.send(JSON.stringify(request));
  };

  return ws;
}
