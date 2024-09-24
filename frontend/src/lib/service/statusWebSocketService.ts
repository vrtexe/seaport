const BASE_URL = 'ws://localhost:8080';

export type DeploymentStatus = (typeof DeploymentStatus)[keyof typeof DeploymentStatus];
export const DeploymentStatus = {
  Ready: 'READY',
  Unknown: 'UNKNOWN',
  Unavailable: 'UNAVAILABLE'
} as const;

export type StatusMessage = {
  id: number;
  status: DeploymentStatus;
};

export function wsConnectStatus(handle: (message: StatusMessage) => unknown) {
  const ws = new WebSocket(`${BASE_URL}/status`);
  ws.onmessage = (message: MessageEvent<string>) => {
    console.log(message.data)
    const messageData = JSON.parse(message.data) as StatusMessage;
    handle(messageData);
  };

  return ws;
}
