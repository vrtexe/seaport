const BASE_URL = 'ws://localhost:8080';

export type MessageType = (typeof MessageType)[keyof typeof MessageType];
export const MessageType = {
  Identifier: 'IDENTIFIER',
  String: 'STRING'
} as const;

export type Message =
  | {
      type: typeof MessageType.Identifier;
      id: string;
    }
  | {
      type: typeof MessageType.String;
      data: string;
    };

export function wsConnectTest(handle: (message: Message) => unknown) {
  const ws = new WebSocket(`${BASE_URL}/test`);
  ws.onmessage = function (message: MessageEvent<string>) {
    const messageData = JSON.parse(message.data) as Message;
    handle(messageData);
  };

  return ws;
}
