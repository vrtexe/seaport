<script lang="ts">
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import ApplicationCreateDialog from '$lib/components/pages/dashboard/applications/ApplicationCreateDialog.svelte';
  import RenameApplicationDialog from '$lib/components/pages/dashboard/applications/RenameApplicationDialog.svelte';
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import { getImages } from '$lib/service/imageService';
  import { onMount } from 'svelte';
  import DangerLinkButton from '$lib/components/buttons/DangerLinkButton.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import Plus from 'svelte-material-icons/Plus.svelte';
  import RenameOutline from 'svelte-material-icons/RenameOutline.svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import ReleaseApplicationTagDialog from '$lib/components/pages/dashboard/applications/ReleaseApplicationTagDialog.svelte';
  import { type ImagesResponse, type Pageable } from '$lib/generated';
  import Pagination from '$lib/components/pages/dashboard/applications/Pagination.svelte';
  import ApplicationBuildStatus from '$lib/components/pages/dashboard/applications/ApplicationBuildStatus.svelte';
  import ApplicationDeleteDialog from '$lib/components/pages/dashboard/applications/ApplicationDeleteDialog.svelte';

  const pageable: Pageable = {
    page: 1,
    size: 20,
    sort: []
  };

  let createDialog: ApplicationCreateDialog;
  let renameDialog: RenameApplicationDialog;
  let releaseTagDialog: ReleaseApplicationTagDialog;
  let deleteDialog: ApplicationDeleteDialog;
  let images: ImagesResponse;

  onMount(() => {
    loadData();
  });

  async function loadData() {
    images = await getImages(pageable);
  }

  function changePage({ detail: page }: CustomEvent<number>) {
    pageable.page = page;
    loadData();
  }
</script>

<div class="flex h-full flex-col gap-8 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="text-2xl font-bold">Applications</h2>
    <PrimaryButton on:click={() => createDialog.open()}>+ New Application</PrimaryButton>
  </div>
  <div class="flex flex-1 flex-col justify-between">
    <table>
      <colgroup>
        <col class="w-0" />
        <col />
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
      </colgroup>
      <thead>
        <tr>
          <th>Status</th>
          <th class="text-left">Name</th>
          <th class="text-left">Latest</th>
          <th>Releases</th>
          <th>Actions</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {#if images}
          {#each images.data as image}
            <tr>
              <td class="text-center">
                {#if image.latest}
                  <ApplicationBuildStatus tag={image.latest} />
                {/if}
              </td>
              <td>{image.name}</td>
              <td>{image.latest?.version ?? ''}</td>
              <td class="text-center">{image.releases}</td>
              <td class="text-center">
                <div class="flex justify-center gap-2">
                  <PrimaryLinkButton on:click={() => renameDialog.open(image.id, { name: image.name })}>
                    <RenameOutline class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Rename</span>
                  </PrimaryLinkButton>
                  <DangerLinkButton on:click={() => deleteDialog.open(image.id)}>
                    <TrashCanOutline class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Delete</span>
                  </DangerLinkButton>
                  <PrimaryLinkButton on:click={() => releaseTagDialog.open(image.id, { data: { name: image.name } })}>
                    <Plus class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Release</span>
                  </PrimaryLinkButton>
                </div>
              </td>
              <td>
                <PrimaryLinkButton href="/application/details/{image.id}">
                  <InformationOutline class="inline-block" height="1.75em" width="1.75em" />
                  <!-- <span class="hidden md:inline-flex items-center ">View</span> -->
                </PrimaryLinkButton>
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

<ApplicationCreateDialog bind:this={createDialog} on:save={loadData} />
<RenameApplicationDialog bind:this={renameDialog} on:save={loadData} />
<ReleaseApplicationTagDialog bind:this={releaseTagDialog} on:save={loadData} />
<ApplicationDeleteDialog bind:this={deleteDialog} on:delete={loadData} />
