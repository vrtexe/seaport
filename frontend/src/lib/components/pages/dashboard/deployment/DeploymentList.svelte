<script lang="ts">
  import DangerLinkButton from '$lib/components/buttons/DangerLinkButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import Pagination from '$lib/components/pages/dashboard/applications/Pagination.svelte';
  import GroupEditDialog from '$lib/components/pages/dashboard/groups/GroupEditDialog.svelte';
  import type { DeploymentsResponse, Pageable } from '$lib/generated';
  import RenameOutline from 'svelte-material-icons/RenameOutline.svelte';
  import TextBox from 'svelte-material-icons/TextBox.svelte';
  import Link from 'svelte-material-icons/Link.svelte';
  import { onMount } from 'svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import { getAllDeployments } from '$lib/service/deploymentService';
  import DeploymentDeleteDialog from '$lib/components/pages/dashboard/deployment/DeploymentDeleteDialog.svelte';
  import DeploymentLogsDialog from '$lib/components/pages/dashboard/deployment/DeploymentLogsDialog.svelte';
  import DeploymentBuildStatus from '$lib/components/pages/dashboard/deployment/DeploymentBuildStatus.svelte';

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
    loadData();
  });

  async function loadData() {
    deployments = await getAllDeployments();
  }

  function changePage({ detail: page }: CustomEvent<number>) {
    pageable.page = page;
    loadData();
  }
</script>

<div class="flex h-full flex-col gap-8 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="text-2xl font-bold">Deployments</h2>
    <PrimaryButton href="/deployment/create">+ Create Deployment</PrimaryButton>
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
          <th></th>
        </tr>
      </thead>
      <tbody>
        {#if deployments}
          {#each deployments.data as deployment}
            <tr>
              <td><DeploymentBuildStatus {deployment} /></td>
              <td>{deployment.name}</td>
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
                http://{deployment.service.name}{deployment.service.port !== 80 ? `:${deployment.service.port}` : ''}
              </td>
              <td>
                {#if deployment.ingress}
                  <PrimaryLinkButton
                    href="{deployment.cluster.url}/{deployment.cluster.namespace}/{deployment.ingress.path}">
                    <Link class="inline-block" height="1.5em" width="1.5em" />
                  </PrimaryLinkButton>
                {/if}
              </td>
              <td class="text-center">
                <div class="flex justify-center gap-2">
                  <PrimaryLinkButton on:click={() => editDialog.open(deployment.id, { name: deployment.name })}>
                    <RenameOutline class="inline-block" height="1.5em" width="1.5em" />
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
              <td>
                <PrimaryLinkButton href="/application/details/{deployment.id}">
                  <InformationOutline class="inline-block" height="1.75em" width="1.75em" />
                </PrimaryLinkButton>
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
