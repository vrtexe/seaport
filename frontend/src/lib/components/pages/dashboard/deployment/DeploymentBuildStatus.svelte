<script lang="ts">
  import { DeploymentState, type Deployment } from '$lib/generated';
  import ClockOutline from 'svelte-material-icons/ClockOutline.svelte';
  import CheckCircleOutline from 'svelte-material-icons/CheckCircleOutline.svelte';
  import AlertCircleOutline from 'svelte-material-icons/AlertCircleOutline.svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import { createEventDispatcher, onDestroy } from 'svelte';
  import Loader from '$lib/components/Loader.svelte';
  import { connectDeploymentStatusWebSocket } from '$lib/service/deploymentWebSocketService';

  const dispatch = createEventDispatcher<{
    change: DeploymentState;
  }>();

  export let deployment: Deployment;

  let status: DeploymentState | undefined = undefined;
  let websocket: WebSocket | undefined;

  function initWebsocket(deployment: Deployment) {
    websocket?.close();
    websocket = connectDeploymentStatusWebSocket({ uid: deployment.uid }, response => {
      status = response.status;
    });
  }

  onDestroy(() => {
    websocket?.close();
    websocket = undefined;
  });

  $: initWebsocket(deployment);
  $: status && dispatch('change', status);
</script>

{#if status == undefined}
  <ClockOutline class="inline-block text-gray-500" height="1.5em" width="1.5em" />
{:else if status === DeploymentState.Started}
  <CheckCircleOutline class="inline-block text-green-600" height="1.5em" width="1.5em" />
{:else if status === DeploymentState.Stopped}
  <InformationOutline class="inline-block text-orange-400" height="1.5em" width="1.5em" />
{:else if status === DeploymentState.Failed}
  <AlertCircleOutline class="inline-block text-red-600" height="1.5em" width="1.5em" />
{:else}
  <Loader class="text-gray-500" />
{/if}
