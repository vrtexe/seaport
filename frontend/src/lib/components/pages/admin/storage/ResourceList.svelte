<script lang="ts">
  import DangerLinkButton from '$lib/components/buttons/DangerLinkButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import Pagination from '$lib/components/pages/dashboard/applications/Pagination.svelte';
  import { BaseImageType, type BaseImageResponse, type Pageable, type StorageResponse } from '$lib/generated';
  import { onMount } from 'svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import PencilOutline from 'svelte-material-icons/PencilOutline.svelte';
  import Select from '$lib/components/Select.svelte';
  import { fetchBuildTools, fetchLanguages, getBaseImages, type BaseImageFilter } from '$lib/service/baseImageService';
  import ImageDelete from '$lib/components/pages/admin/image/ImageDelete.svelte';
  import ImageDetails from '$lib/components/pages/admin/image/ImageDetails.svelte';
  import { getStoredResources, type StorageCriteriaFilter } from '$lib/service/storageService';
  import Input from '$lib/components/Input.svelte';
  import ResourceDelete from '$lib/components/pages/admin/storage/ResourceDelete.svelte';
  import { BASE_URL, BASE_URL_WITHOUT_PATH } from '$lib/config';
  import LinkButton from '$lib/components/sidebar/LinkButton.svelte';

  const pageable: Pageable = {
    page: 1,
    size: 20,
    sort: []
  };

  let deleteDialog: ResourceDelete;

  let data: StorageResponse | undefined = undefined;

  let filterCriteria: StorageCriteriaFilter = { name: '' };

  onMount(() => {
    loadData();
  });

  async function loadData() {
    data = await getStoredResources(filterCriteria, pageable);
  }

  function changePage({ detail: page }: CustomEvent<number>) {
    pageable.page = page;
    loadData();
  }

  $: filterCriteria, loadData();
</script>

<div class="flex h-full flex-col gap-8 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="text-2xl font-bold">Stored resources</h2>
  </div>
  <div class="grid grid-cols-3 gap-4">
    {#if filterCriteria.name != undefined}
      <Input id="language-filter" name="language-filter" bind:value={filterCriteria.name}>File</Input>
    {/if}
  </div>
  <div class="flex flex-1 flex-col justify-between">
    <table>
      <colgroup>
        <col class="w-auto" />
        <col class="w-auto" />
        <col class="w-auto" />
        <col class="w-auto" />
        <col class="w-0" />
        <col class="w-0" />
      </colgroup>
      <thead>
        <tr>
          <th class="text-left">Name</th>
          <th class="text-left">Path</th>
          <th class="text-left">Size (bytes)</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {#if data}
          {#each data.data as item}
            <tr>
              <td>
                <PrimaryLinkButton href="{BASE_URL_WITHOUT_PATH}{item.href}">
                  {item.displayName ?? item.name}
                </PrimaryLinkButton>
              </td>
              <td>
                {item.path}
              </td>
              <td>
                {item.contentLength}
              </td>
              <td class="text-center">
                <div class="flex justify-center gap-2">
                  <DangerLinkButton on:click={() => deleteDialog.open(item.href)}>
                    <TrashCanOutline class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Delete</span>
                  </DangerLinkButton>
                </div>
              </td>
            </tr>
          {/each}
        {/if}
      </tbody>
    </table>
    {#if data?.metadata}
      <div>
        <Pagination value={data.metadata.pagination} on:page={changePage} />
      </div>
    {/if}
  </div>
</div>

<ResourceDelete bind:this={deleteDialog} on:save={loadData} />
