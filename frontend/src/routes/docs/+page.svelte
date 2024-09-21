<script lang="ts">
  import { onMount } from 'svelte';

  let ws: WebSocket;

  let out = '';
  let connected = false;

  type MessageType = (typeof MessageType)[keyof typeof MessageType];
  const MessageType = {
    Identifier: 'IDENTIFIER',
    String: 'STRING'
  } as const;

  type Message =
    | {
        type: typeof MessageType.Identifier;
        id: string;
      }
    | {
        type: typeof MessageType.String;
        data: string;
      };
  onMount(() => {
    connect();
    return () => {
      disconnect();
    };
  });

  function connect() {
    ws = new WebSocket('ws://localhost:8080/test');
    ws.onmessage = function (message: MessageEvent<string>) {
      const messageData = JSON.parse(message.data) as Message;
      if (messageData.type === MessageType.Identifier) {
        console.log(messageData.id);
      } else {
        console.log('here', messageData);
      }
    };
  }

  function disconnect() {
    ws?.close();
    console.log('Disconnected');
  }

</script>

Exploring

<button class="primary" on:click={() => ws.send(`{"name": "Test"}`)}> Send Message </button>

{JSON.stringify(out)}
