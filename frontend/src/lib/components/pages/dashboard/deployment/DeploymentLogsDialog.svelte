<script lang="ts">
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import AnsiTexts, { parseAnsi } from '$lib/components/pages/dashboard/applications/AnsiTexts.svelte';
  import { connectDeploymentLogWebSocket } from '$lib/service/deploymentWebSocketService';
  import { onDestroy } from 'svelte';

  type LogAggregate = { data: string };

  let dialog: DefaultDialog;
  let log: LogAggregate = defaultLogAggregate();
  let websocket: WebSocket | undefined;

  export const open = async (uid: string) => {
    initWebsocket(uid);
    dialog.open();
  };

  export const closeDialog = () => {
    log = defaultLogAggregate();

    dialog.close();
    websocket?.close();
  };

  function defaultLogAggregate(): LogAggregate {
    return { data: '' };
  }

  function initWebsocket(uid: string) {
    websocket?.close();
    websocket = connectDeploymentLogWebSocket({ uid: uid }, response => {
      console.log(response.data)
      log.data = [log.data, response.data].filter(s => s).join('\n');
    });
  }

  onDestroy(() => {
    websocket?.close();
  });

  $: lines = log?.data ? log.data.split('\n').toReversed() : [];
</script>

<DefaultDialog height="80%" bind:this={dialog}>
  <svelte:fragment slot="title">Deployment log</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="flex min-w-[60rem] max-w-[90rem] flex-col-reverse">
      {#if lines.length}
        {#each lines as line, i}
          <div class="flex gap-x-6 font-mono">
            <span class="flex w-4 select-none items-center text-sm font-bold opacity-25">{lines.length - i}</span>
            <span>
                <pre><AnsiTexts values={parseAnsi(line)} /></pre>
            </span>
          </div>
        {/each}
      {/if}
    </div>
  </svelte:fragment>
  <svelte:fragment slot="actions">
    <div>
      <PrimaryButton on:click={closeDialog}>Close</PrimaryButton>
    </div>
  </svelte:fragment>
</DefaultDialog>
