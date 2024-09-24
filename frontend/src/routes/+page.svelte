<script lang="ts">
  import {
    getApplications,
    stopDeployment,
    startDeployment,
    updateDeployment,
    editDeployment
  } from '$lib/service/applicationService';
  import {
    ImageTypeDto,
    type EnvironmentDto,
    type IngressRuleDto,
    type NamespaceDto
  } from '$lib/service/applicationServiceDto';
  import { onDestroy, onMount } from 'svelte';
  import Package from 'svelte-material-icons/Package.svelte';
  import PencilOutline from 'svelte-material-icons/PencilOutline.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import Close from 'svelte-material-icons/Close.svelte';
  import ModalDialog from '$lib/components/ModalDialog.svelte';
  import { wsConnectStatus, type DeploymentStatus, type StatusMessage } from '$lib/service/statusWebSocketService';
  import Input from '$lib/components/Input.svelte';
  import ServicePropertiesEdit, { type ApplicationProperty } from '$lib/components/ServicePropertiesEdit.svelte';

  const HTTP_PORT = 80;
  const HTTPS_PORT = 443;
  const PROTOCOL_PORTS = [HTTPS_PORT, HTTP_PORT];

  const BASE_CLUSTER_URL = 'http://localhost';

  let namespace: NamespaceDto | undefined;
  let ws: WebSocket;

  onMount(() => {
    ws = wsConnectStatus(message);
    requestApplications();
  });

  onDestroy(() => {
    try {
      ws?.close();
    } catch (e) {
      console.error(e);
    }
  });

  const deploymentStatus: Record<number, DeploymentStatus> = {};

  function message(message: StatusMessage) {
    deploymentStatus[message.id] = message.status;
  }

  const requestApplications = async () => {
    namespace = await getApplications('vangel');
    const ids = namespace.applications.flatMap(a => a.deployments.map(d => d.id));
    ws.send(`{"ids": [${ids.join(',')}] }`);
  };

  const resolveInternalUrl = (ingressRule: IngressRuleDto) => {
    const service = ingressRule.service;
    const servicePort = service.port[0].port;

    return `${resolveProtocol(servicePort)}://${service.name}${resolvePort(servicePort)}`;
  };

  const resolvePort = (port: number) => {
    return PROTOCOL_PORTS.includes(port) ? '' : `:${port}`;
  };

  const resolveProtocol = (port: number) => {
    switch (port) {
      case HTTP_PORT:
        return 'http';
      case HTTPS_PORT:
        return 'https';
    }

    return 'http';
  };

  let environmentDto: EnvironmentDto | undefined;

  const openEnvDialog = (environment: EnvironmentDto) => {
    environmentDto = environment;
    dialog?.showModal();
  };

  let dialog: HTMLDialogElement;

  let updateDialog: HTMLDialogElement;
  let updateDialogData: {
    id?: number;
    version: string;
  } = { version: '' };

  const openUpdateDialog = (id: number) => {
    updateDialogData.id = id;
    updateDialog.showModal();
  };

  const saveAndClose = () => {
    if (!updateDialogData.id || !updateDialogData.version) {
      return;
    }

    updateDeployment(updateDialogData.id, updateDialogData.version);
    closeUpdateDialog();
  };

  const closeUpdateDialog = () => {
    updateDialog.close();
    updateDialogData = { version: '' };
  };

  let editDialog: HTMLDialogElement;
  let editDialogData: {
    id?: number;
    properties: ApplicationProperty[];
  } = { properties: [] };

  const openEditDialog = (id: number, properties: Record<string, string>) => {
    console.log(id);
    editDialogData.id = id;
    editDialogData.properties = Object.entries(properties).map(([key, value]) => ({
      name: key,
      value: value,
      custom: true
    }));
    editDialog.showModal();
  };

  const saveAndCloseEditDialog = () => {
    if (!editDialogData.id) return;
    const properties = Object.fromEntries(editDialogData.properties.map(p => [p.name, p.value]));
    editDeployment(editDialogData.id, properties);
    closeEditDialog();
  };

  const closeEditDialog = () => {
    editDialog.close();
    editDialogData = { properties: [] };
  };

  $: baseImages =
    namespace &&
    Object.fromEntries(namespace.applications.map(a => [a.id, Object.fromEntries(a.baseImages.map(i => [i.id, i]))]));
</script>

<main class="flex w-full flex-col justify-center">
  {#if namespace?.applications}
    <div class="flex justify-end px-4 py-2">
      <a href="/application/create" class="primary-button text-xl">New Application</a>
    </div>
    <div class="grid grid-cols-3 gap-8 p-10">
      {#each namespace.applications as app}
        <div class="rounded-md border px-4 py-2">
          <div class="mb-2 flex justify-between">
            <span class="rounded-md py-2 text-xl">{app.name}</span>
            <span class="flex gap-2 text-2xl">
              <!-- <button class="rounded-md bg-gray-100 px-2 text-black"><PencilOutline size="1em" /></button> -->
              <!-- <button class="rounded-md bg-red-400 px-2 text-white"><TrashCanOutline size="1em" /></button> -->
            </span>
          </div>
          <div class="flex flex-wrap">
            {#each app.deployments as deployment}
              <div class="w-full rounded-md border px-4 py-4">
                <div class="flex justify-between">
                  <span class="mb-2 inline-flex items-center gap-2 rounded-md bg-gray-100 px-4 py-2">
                    {deployment.name}
                    <span class="status" data-status={deploymentStatus[deployment.id]}></span>
                  </span>
                  <span class="grid gap-1">
                    <a
                      href="{BASE_CLUSTER_URL}/{deployment.ingresses[0].ingressRules[0].path}"
                      target="_blank"
                      class="h-fit cursor-pointer rounded-md border bg-gray-100 px-2 py-1 text-center text-xs font-bold opacity-60 hover:opacity-100">
                      Link
                    </a>
                    <button
                      on:click={() => openEnvDialog(deployment.pods[0].environment)}
                      class="h-fit rounded-md border bg-gray-100 px-2 py-1 text-xs font-bold opacity-60 hover:opacity-100">
                      Config
                    </button>
                  </span>
                </div>

                {#each deployment.pods as pod}
                  {@const baseImage = baseImages?.[app.id][pod.image.baseRef]}
                  <div class="pt-4">
                    <div class="py-1">
                      <span class="rounded-md bg-sky-100 px-4 py-1 text-sm font-semibold">
                        {pod.image.name}:{pod.image.version}
                      </span>
                      {#if baseImage}
                        <span class="rounded-md bg-sky-100 px-4 py-1 text-sm font-semibold">
                          {baseImage.type}
                        </span>
                        <span class="rounded-md bg-sky-100 px-4 py-1 text-sm font-semibold">
                          {`${baseImage.language} ${baseImage.languageVersion}`}
                        </span>
                        {#if baseImage.type === ImageTypeDto.Git}
                          <span class="rounded-md bg-sky-100 px-4 py-1 text-sm font-semibold">
                            {`${baseImage.buildTool} ${baseImage.buildToolVersion}`}
                          </span>
                        {/if}
                      {/if}
                    </div>
                    <div class="py-1">
                      {#if pod.image.arguments['URL']}
                        <a class="text-xs font-bold text-blue-400" href={pod.image.arguments['URL']}>
                          {pod.image.arguments['URL']}
                        </a>
                      {/if}
                    </div>
                    <div class="bordered grid grid-cols-1 py-1">
                      <div class="inline-grid grid-cols-[10rem_1fr] py-1">
                        <span>Port</span> <span>{pod.port}</span>
                      </div>
                      {#each deployment.ingresses as ingress}
                        {#each ingress.ingressRules as ingressRule}
                          {@const url = `${BASE_CLUSTER_URL}/${ingressRule.path}`}
                          <div class="inline-grid grid-cols-[10rem_1fr] py-1">
                            <span>External</span>
                            <span> <a class="text-blue-400" target="_blank" href={url}>{url}</a></span>
                          </div>

                          <div class="inline-grid grid-cols-[10rem_1fr] py-1">
                            <span>Internal</span>
                            <span>{resolveInternalUrl(ingressRule)}</span>
                          </div>
                        {/each}
                      {/each}
                    </div>
                    <div class="mt-4 grid grid-cols-4">
                      <span>
                        <button
                          class="w-full py-2 text-blue-500"
                          on:click={() => {
                            deploymentStatus[deployment.id] !== 'READY'
                              ? startDeployment(deployment.id)
                              : stopDeployment(deployment.id);
                          }}>
                          {deploymentStatus[deployment.id] !== 'READY' ? 'Start' : 'Stop'}
                        </button>
                      </span>
                      <span>
                        <button
                          class=" w-full py-2 text-blue-500"
                          on:click={() => openEditDialog(deployment.id, pod.environment.content)}>Edit</button>
                      </span>
                      <!-- <span>
                        <button class="w-full py-2 text-blue-500">View</button>
                      </span> -->
                      <span>
                        <button class="w-full py-2 text-blue-500" on:click={() => openUpdateDialog(deployment.id)}>
                          Update
                        </button>
                      </span>
                      <span>
                        <button class="w-full py-2 text-red-500">Delete</button>
                      </span>
                    </div>
                  </div>
                {/each}
              </div>
            {/each}
          </div>
        </div>
      {/each}
    </div>
  {:else}
    <div class="flex h-fit flex-col gap-6 py-32">
      <div class="flex justify-center"><Package class="text-[5em]" /></div>
      <h2 class="text-center text-2xl">You have not yet hosted any applications</h2>
      <div class="flex justify-center gap-8">
        <a href="/application/create" class="primary-button text-xl">Host your first application</a>
        <a href="/docs" class="primary-button text-xl">Read documentation</a>
      </div>
    </div>
  {/if}
</main>

<ModalDialog bind:dialog>
  <div class="grid min-h-[30rem] min-w-[40rem] grid-rows-[auto_1fr_auto]">
    <div class="flex select-none justify-between border-b bg-gray-100 px-3 py-2">
      <span class="text-lg font-semibold">Config</span>
      <span>
        <button class="h-full focus:outline-none">
          <Close size="1.5em" />
        </button>
      </span>
    </div>
    <div class="px-4 py-2">
      {#if environmentDto?.content && Object.keys(environmentDto?.content).length}
        <div>
          {#each Object.entries(environmentDto.content) as [name, value]}
            <span>{name}</span>
            <span>{value}</span>
          {/each}
        </div>
      {:else}
        No custom config set
      {/if}
    </div>
    <div class="flex justify-end gap-2 border-t px-3 py-2">
      <button class="secondary" on:click={() => dialog?.close()}>Close</button>
    </div>
  </div>
</ModalDialog>

<ModalDialog bind:dialog={updateDialog}>
  <div class="grid min-h-[30rem] min-w-[40rem] grid-rows-[auto_1fr_auto]">
    <div class="flex select-none justify-between border-b bg-gray-100 px-3 py-2">
      <span class="text-lg font-semibold">Update</span>
      <span>
        <button class="h-full focus:outline-none" on:click={closeUpdateDialog}>
          <Close size="1.5em" />
        </button>
      </span>
    </div>
    <div class="px-4 py-2">
      <Input id="update-version" name="update-version" bind:value={updateDialogData.version} placeholder="1.0.1">
        Version
      </Input>
    </div>
    <div class="flex justify-end gap-2 border-t px-3 py-2">
      <button class="secondary" on:click={closeUpdateDialog}>Close</button>
      <button disabled={!updateDialogData.version} class="primary" on:click={saveAndClose}>Save</button>
    </div>
  </div>
</ModalDialog>

<ModalDialog bind:dialog={editDialog}>
  <div class="grid max-h-[30rem] min-h-[30rem] min-w-[50rem] max-w-[50rem] grid-rows-[auto_1fr_auto]">
    <div class="flex select-none justify-between border-b bg-gray-100 px-3 py-2">
      <span class="text-lg font-semibold">Update</span>
      <span>
        <button class="h-full focus:outline-none" on:click={closeEditDialog}>
          <Close size="1.5em" />
        </button>
      </span>
    </div>
    <div class="overflow-auto px-4 py-2">
      <ServicePropertiesEdit bind:applicationProperties={editDialogData.properties} />
    </div>
    <div class="flex justify-end gap-2 border-t px-3 py-2">
      <button class="secondary" on:click={closeEditDialog}>Close</button>
      <button class="primary" on:click={saveAndCloseEditDialog}>Save</button>
    </div>
  </div>
</ModalDialog>

<style lang="postcss">
  .bordered > div:not(:last-child) {
    @apply border-b;
  }

  .status {
    width: 1em;
    height: 1em;

    @apply inline-block rounded-full;
  }
  .status[data-status='READY'] {
    @apply bg-green-300;
  }

  .status[data-status='UNKNOWN'] {
    @apply bg-gray-500;
  }

  .status[data-status='UNAVAILABLE'] {
    @apply bg-red-400;
  }
</style>
