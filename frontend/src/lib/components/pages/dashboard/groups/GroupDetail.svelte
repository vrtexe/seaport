<script lang="ts">
  import DangerLinkButton from '$lib/components/buttons/DangerLinkButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import Pagination from '$lib/components/pages/dashboard/applications/Pagination.svelte';
  import GroupCreateDialog from '$lib/components/pages/dashboard/groups/GroupCreateDialog.svelte';
  import GroupDeleteDialog from '$lib/components/pages/dashboard/groups/GroupDeleteDialog.svelte';
  import GroupEditDialog from '$lib/components/pages/dashboard/groups/GroupEditDialog.svelte';
  import type { GroupsResponse, Pageable } from '$lib/generated';
  import { getAllGroups } from '$lib/service/groupService';
  import RenameOutline from 'svelte-material-icons/RenameOutline.svelte';
  import { onMount } from 'svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';

  const pageable: Pageable = {
    page: 1,
    size: 20,
    sort: []
  };

  let createDialog: GroupCreateDialog;
  let editDialog: GroupEditDialog;
  let deleteDialog: GroupDeleteDialog;

  let groups: GroupsResponse | undefined = undefined;

  onMount(() => {
    loadData();
  });

  async function loadData() {
    groups = await getAllGroups(pageable);
  }

  function changePage({ detail: page }: CustomEvent<number>) {
    pageable.page = page;
    loadData();
  }
</script>

<div class="flex h-full flex-col gap-8 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="text-2xl font-bold">Groups</h2>
    <PrimaryButton on:click={() => createDialog.open()}>+ Create Group</PrimaryButton>
  </div>
  <div class="flex flex-1 flex-col justify-between">
    <table>
      <colgroup>
        <col />
        <col class="w-0" />
        <col class="w-0" />
      </colgroup>
      <thead>
        <tr>
          <th class="text-left">Name</th>
          <th>Actions</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {#if groups}
          {#each groups.data as group}
            <tr>
              <td>{group.name}</td>
              <td class="text-center">
                <div class="flex justify-center gap-2">
                  <PrimaryLinkButton on:click={() => editDialog.open(group.id, { name: group.name })}>
                    <RenameOutline class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Rename</span>
                  </PrimaryLinkButton>
                  <DangerLinkButton on:click={() => deleteDialog.open(group.id)}>
                    <TrashCanOutline class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Delete</span>
                  </DangerLinkButton>
                </div>
              </td>
              <td>
                <PrimaryLinkButton href="/application/details/{group.id}">
                  <InformationOutline class="inline-block" height="1.75em" width="1.75em" />
                </PrimaryLinkButton>
              </td>
            </tr>
          {/each}
        {/if}
      </tbody>
    </table>
    {#if groups}
      <div>
        <Pagination value={groups.metadata.pagination} on:page={changePage} />
      </div>
    {/if}
  </div>
</div>

<GroupCreateDialog bind:this={createDialog} on:save={loadData} />
<GroupEditDialog bind:this={editDialog} on:save={loadData} />
<GroupDeleteDialog bind:this={deleteDialog} on:save={loadData} />
