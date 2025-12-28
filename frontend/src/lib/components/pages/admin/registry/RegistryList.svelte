<script lang="ts">
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import DangerLinkButton from '$lib/components/buttons/DangerLinkButton.svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import Pagination from '$lib/components/pages/dashboard/applications/Pagination.svelte';
  import { type ImagesResponse, type Pageable } from '$lib/generated';
  import { getRegistryImages } from '$lib/service/registryService';
  import { onMount } from 'svelte';
  import RegistryDeleteDialog from '$lib/components/pages/admin/registry/RegistryDeleteDialog.svelte';
  import Input from '$lib/components/Input.svelte';

  const pageable: Pageable = {
    page: 1,
    size: 20,
    sort: []
  };

  let images: ImagesResponse | undefined;
  let deleteDialog: RegistryDeleteDialog;
  let filter: { user: string; app: string; namespace: string } = { user: '', app: '', namespace: '' };

  onMount(loadData);

  async function loadData() {
    const query = {
      user: filter.user ? filter.user : undefined,
      app: filter.app ? filter.app : undefined,
      namespace: filter.namespace ? filter.namespace : undefined
    };
    images = await getRegistryImages(query, pageable);
  }

  $: filter, loadData();

  function changePage({ detail: page }: CustomEvent<number>) {
    pageable.page = page;
    loadData();
  }

  function formatUser(user?: { username?: string; firstName?: string; lastName?: string; uid: string }) {
    if (!user) return '';
    if (user.username) return user.username;
    const name = [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
    return name || user.uid;
  }
</script>

<div class="flex h-full flex-col gap-8 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="text-2xl font-bold">Registry</h2>
  </div>
  <div class="grid grid-cols-3 gap-4">
    <Input id="filter-user" name="filter-user" bind:value={filter.user}>User</Input>
    <Input id="filter-app" name="filter-app" bind:value={filter.app}>App</Input>
    <Input id="filter-namespace" name="filter-namespace" bind:value={filter.namespace}>Namespace</Input>
  </div>
  <div class="flex flex-1 flex-col justify-between">
    <table>
      <colgroup>
        <col />
        <col />
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
      </colgroup>
      <thead>
        <tr>
          <th class="text-left">Name</th>
          <th class="text-left">User</th>
          <th class="text-left">Latest</th>
          <th>Releases</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {#if images}
          {#each images.data as image}
            <tr>
              <td>{image.name}</td>
              <td>{formatUser(image.user)}</td>
              <td>{image.latest?.version ?? ''}</td>
              <td class="text-center">{image.releases}</td>
              <td class="text-center">
                <div class="flex justify-center gap-2">
                  <DangerLinkButton on:click={() => deleteDialog.open(image.id)}>
                    <TrashCanOutline class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Delete</span>
                  </DangerLinkButton>
                  <PrimaryLinkButton href="/admin/registry/details/{image.id}">
                    <InformationOutline class="inline-block" height="1.75em" width="1.75em" />
                    <span class="hidden lg:inline-block">Details</span>
                  </PrimaryLinkButton>
                </div>
              </td>
            </tr>
          {/each}
        {/if}
      </tbody>
    </table>
    {#if images}
      <div>
        <Pagination value={images.metadata.pagination} on:page={changePage} />
      </div>
    {/if}
  </div>
</div>

<RegistryDeleteDialog bind:this={deleteDialog} on:delete={loadData} />
