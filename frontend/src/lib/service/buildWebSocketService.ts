const BASE_URL = 'ws://localhost:8081';
// const BASE_URL = 'ws://localhost/app';

export type DeploymentStatus = (typeof DeploymentStatus)[keyof typeof DeploymentStatus];
export const DeploymentStatus = Object.freeze({
  Initialized: "INITIALIZED",
  Started: "STARTED",
  Completed: "COMPLETED",
  Failed: "FAILED"
} as const);

export type StatusMessageResponse = {
  status: DeploymentStatus;
};

export type LogMessageResponse = {
  status: DeploymentStatus;
  data: string;
};

export type BuildStatusMessage = {
  uid: string;
};

export function connectBuildStatusWebSocket(request: BuildStatusMessage, handle: (message: StatusMessageResponse) => unknown) {
  const ws = new WebSocket(`${BASE_URL}/build/status`);  
  ws.onmessage = (message: MessageEvent<string>) => {
    const messageData = JSON.parse(message.data) as StatusMessageResponse;
    handle(messageData);
  };

  ws.onopen = () => {
    ws.send(JSON.stringify(request));
  }

  return ws;
}

export function connectBuildLogWebSocket(request: BuildStatusMessage, handle: (message: LogMessageResponse) => unknown) {
  const ws = new WebSocket(`${BASE_URL}/build/logs`);  
  ws.onmessage = (message: MessageEvent<string>) => {
    const messageData = JSON.parse(message.data) as LogMessageResponse;
    handle(messageData);
  };

  ws.onopen = () => {
    ws.send(JSON.stringify(request));
  }

  return ws;
}