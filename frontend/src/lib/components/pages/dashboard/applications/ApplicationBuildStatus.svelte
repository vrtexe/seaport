<script lang="ts">
  import { ImageTagStatus, type ImageTag } from '$lib/generated';
  import ClockOutline from 'svelte-material-icons/ClockOutline.svelte';
  import CheckCircleOutline from 'svelte-material-icons/CheckCircleOutline.svelte';
  import AlertCircleOutline from 'svelte-material-icons/AlertCircleOutline.svelte';
  import { connectBuildStatusWebSocket } from '$lib/service/buildWebSocketService';
  import { onDestroy, onMount } from 'svelte';
  import Loader from '$lib/components/Loader.svelte';

  const doneStatuses = <ImageTagStatus[]>[ImageTagStatus.Completed, ImageTagStatus.Failed];

  export let tag: ImageTag;

  let realtimeStatus: ImageTagStatus = tag.status;
  let websocket: WebSocket | undefined;

  function initWebsocket(tag: ImageTag) {
    websocket?.close();
    if (doneStatuses.includes(tag.status)) {
      return;
    }
    websocket = connectBuildStatusWebSocket({ uid: tag.uid }, response => {
      realtimeStatus = response.status;
    });
  }

  onDestroy(() => {
    websocket?.close();
  });

  $: initWebsocket(tag);
  $: status = realtimeStatus ?? tag.status;
  $: doneStatuses.includes(realtimeStatus) && (tag.status = realtimeStatus);
</script>

{#if status === ImageTagStatus.Initialized}
  <ClockOutline class="inline-block text-gray-500" height="1.5em" width="1.5em" />
{:else if status === ImageTagStatus.Completed}
  <CheckCircleOutline class="inline-block text-green-600" height="1.5em" width="1.5em" />
{:else if status === ImageTagStatus.Failed}
  <AlertCircleOutline class="inline-block text-red-600" height="1.5em" width="1.5em" />
{:else if status === ImageTagStatus.Started}
  <Loader class="text-gray-500" />
{/if}
