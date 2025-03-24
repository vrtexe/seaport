<script lang="ts">
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import AnsiTexts, { parseAnsi } from '$lib/components/pages/dashboard/applications/AnsiTexts.svelte';
  import { ImageTagStatus, type ImageLog, type ImageTag, type ImageTagDetails } from '$lib/generated';
  import { connectBuildLogWebSocket, connectBuildStatusWebSocket } from '$lib/service/buildWebSocketService';
  import { getImageLogs } from '$lib/service/imageService';
  import { onDestroy } from 'svelte';

  const doneStatuses = <ImageTagStatus[]>[ImageTagStatus.Completed, ImageTagStatus.Failed];

  let dialog: DefaultDialog;
  let log: ImageLog | undefined;
  let tag: ImageTagDetails | undefined;

  export const open = async (newTag: ImageTagDetails) => {
    tag = newTag;
    if (doneStatuses.includes(tag.status)) {
      log = await getImageLogs(tag.id);
    } else {
      log = { data: '' };
      initWebsocket(tag);
    }
    dialog.open();
  };

  export const closeDialog = () => {
    log = undefined;
    tag = undefined;
    dialog.close();
    websocket?.close();
  };

  let realtimeStatus: ImageTagStatus | undefined = tag?.status;
  let websocket: WebSocket | undefined;

  function initWebsocket(tag: ImageTagDetails) {
    websocket?.close();
    if (doneStatuses.includes(tag.status)) {
      return;
    }

    websocket = connectBuildLogWebSocket({ uid: tag.uid }, response => {
      realtimeStatus = response.status;
      tag.status = response.status;
      if (!log) return;
      log.data = [log.data, response.data].filter(s => s).join('\n');
    });
  }

  onDestroy(() => {
    websocket?.close();
  });

  function handleStatusChange(status: ImageTagStatus | undefined) {
    if (!status || doneStatuses.includes(status)) websocket?.close();
  }

  $: handleStatusChange(realtimeStatus);
  $: realtimeStatus && doneStatuses.includes(realtimeStatus) && tag && (tag.status = realtimeStatus)
  $: lines = log?.data ? log.data.split('\n').toReversed() : [];
</script>

<DefaultDialog height="80%" bind:this={dialog}>
  <svelte:fragment slot="title">Application build log</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="flex flex-col-reverse min-w-[60rem] max-w-[90rem]">
      {#if lines.length}
        {#each lines as line, i}
        <div class="flex gap-x-2">
          <span class="select-none text-sm font-bold opacity-25 w-4">{lines.length - i}</span>
          <span>
            <AnsiTexts values={parseAnsi(line)} />
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
