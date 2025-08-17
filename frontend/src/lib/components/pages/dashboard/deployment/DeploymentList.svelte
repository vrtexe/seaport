<script lang="ts">
  import DangerLinkButton from '$lib/components/buttons/DangerLinkButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import Pagination from '$lib/components/pages/dashboard/applications/Pagination.svelte';
  import GroupEditDialog from '$lib/components/pages/dashboard/groups/GroupEditDialog.svelte';
  import { DeploymentState, type DeploymentsResponse, type Group, type Pageable } from '$lib/generated';
  import TextBox from 'svelte-material-icons/TextBox.svelte';
  import Link from 'svelte-material-icons/Link.svelte';
  import PencilOutline from 'svelte-material-icons/PencilOutline.svelte';
  import { onMount } from 'svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import PlayCircleOutline from 'svelte-material-icons/PlayCircleOutline.svelte';
  import StopCircleOutline from 'svelte-material-icons/StopCircleOutline.svelte';
  import { getAllDeployments, updateDeploymentState } from '$lib/service/deploymentService';
  import DeploymentDeleteDialog from '$lib/components/pages/dashboard/deployment/DeploymentDeleteDialog.svelte';
  import DeploymentLogsDialog from '$lib/components/pages/dashboard/deployment/DeploymentLogsDialog.svelte';
  import DeploymentBuildStatus from '$lib/components/pages/dashboard/deployment/DeploymentBuildStatus.svelte';
  import Select from '$lib/components/Select.svelte';
  import { getAllGroups } from '$lib/service/groupService';
  import { page } from '$app/stores';

  const pageable: Pageable = {
    page: 1,
    size: 20,
    sort: []
  };

  let editDialog: GroupEditDialog;
  let deleteDialog: DeploymentDeleteDialog;
  let logsDialog: DeploymentLogsDialog;

  let deployments: DeploymentsResponse | undefined = undefined;

  onMount(() => {
    // console.log()
    group = $page.url.searchParams.get('group') || undefined;
    loadData();
    updateGroups();
  });

  async function loadData() {
    deployments = await getAllDeployments(group);
  }

  function changePage({ detail: page }: CustomEvent<number>) {
    pageable.page = page;
    loadData();
  }

  let deploymentInProgress = false;
  function handleStateUpdate(id: number, state: DeploymentState | undefined) {
    if (deploymentInProgress) return;
    deploymentInProgress = true;
    updateDeploymentState(id, reverseState(state)).finally(() => (deploymentInProgress = false));
  }

  const DeploymentStateReverseMap: Record<DeploymentState, DeploymentState> = {
    [DeploymentState.Initial]: DeploymentState.Started,
    [DeploymentState.Failed]: DeploymentState.Started,
    [DeploymentState.Stopped]: DeploymentState.Started,
    [DeploymentState.Started]: DeploymentState.Stopped
  };

  function reverseState(state: DeploymentState | undefined): DeploymentState {
    return state ? DeploymentStateReverseMap[state] : DeploymentState.Started;
  }

  let groups: Group[] = [];
  let group: string | undefined;
  async function updateGroups() {
    groups = (await getAllGroups()).data;
    group = groups.find(g => g.name === group)?.name;
  }

  $: group, loadData();
</script>

<div class="flex h-full flex-col gap-8 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="text-2xl font-bold">Deployments</h2>
    <PrimaryButton href="/deployment/create">+ Create Deployment</PrimaryButton>
  </div>
  <div class="grid grid-cols-4">
    <Select
      id="deployment-group"
      name="deployment-group"
      options={groups.map(s => ({ name: s.name, value: s.name }))}
      bind:value={group}>
      Group
    </Select>
  </div>
  <div class="flex flex-1 flex-col justify-between">
    <table>
      <colgroup>
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
        <col />
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
      </colgroup>
      <thead>
        <tr>
          <th>Status</th>
          <th class="text-left">Name</th>
          <th class="text-left">Group</th>
          <th class="text-left">Application</th>
          <th class="text-left">Version</th>
          <th>Service</th>
          <th>External</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {#if deployments}
          {#each deployments.data as deployment}
            <tr>
              <td><DeploymentBuildStatus {deployment} on:change={e => (deployment.state = e.detail)} /></td>
              <td>
                <span class="text-nowrap">{deployment.name}</span>
              </td>
              <td>
                <span class="text-nowrap">
                  <PrimaryLinkButton>
                    {deployment.group?.name ?? ''}
                  </PrimaryLinkButton>
                </span>
              </td>
              <td>
                <PrimaryLinkButton href="/application/details/{deployment.image.id}">
                  {deployment.image.name}
                </PrimaryLinkButton>
              </td>
              <td>{deployment.image.tag.version}</td>
              <td>
                <span class="text-nowrap">
                  http://{deployment.service.name}{deployment.service.port !== 80 ? `:${deployment.service.port}` : ''}
                </span>
              </td>
              <td>
                {#if deployment.ingress}
                  <PrimaryLinkButton
                    href="{deployment.cluster.url}/{deployment.cluster.namespace}/{deployment.ingress.path}"
                    target="_blank">
                    <Link class="inline-block" height="1.5em" width="1.5em" />
                  </PrimaryLinkButton>
                {/if}
              </td>
              <td class="text-center">
                <div class="flex justify-center gap-2">
                  <PrimaryLinkButton
                    disabled={deploymentInProgress}
                    on:click={() => handleStateUpdate(deployment.id, deployment.state)}>
                    {#if deployment.state === DeploymentState.Started}
                      <StopCircleOutline class="inline-block" height="1.5em" width="1.5em" />
                    {:else}
                      <PlayCircleOutline class="inline-block" height="1.5em" width="1.5em" />
                    {/if}
                    <!-- <span class="hidden lg:inline-block">Rename</span> -->
                  </PrimaryLinkButton>
                  <PrimaryLinkButton href="/deployment/edit/{deployment.id}">
                    <PencilOutline class="inline-block" height="1.5em" width="1.5em" />
                    <!-- <span class="hidden lg:inline-block">Rename</span> -->
                  </PrimaryLinkButton>
                  <PrimaryLinkButton on:click={() => logsDialog.open(deployment.uid)}>
                    <TextBox class="inline-block" height="1.5em" width="1.5em" />
                    <!-- <span class="hidden lg:inline-block">Logs</span> -->
                  </PrimaryLinkButton>
                  <DangerLinkButton on:click={() => deleteDialog.open(deployment.id)}>
                    <TrashCanOutline class="inline-block" height="1.5em" width="1.5em" />
                    <!-- <span class="hidden lg:inline-block">Delete</span> -->
                  </DangerLinkButton>
                </div>
              </td>
            </tr>
          {/each}
        {/if}
      </tbody>
    </table>
    {#if deployments?.metadata}
      <div>
        <Pagination value={deployments.metadata.pagination} on:page={changePage} />
      </div>
    {/if}
  </div>
</div>

<GroupEditDialog bind:this={editDialog} on:save={loadData} />
<DeploymentDeleteDialog bind:this={deleteDialog} on:save={loadData} />
<DeploymentLogsDialog bind:this={logsDialog} />
