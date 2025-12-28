<script lang="ts">
  import DangerLinkButton from '$lib/components/buttons/DangerLinkButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import Pagination from '$lib/components/pages/dashboard/applications/Pagination.svelte';
  import { BaseImageType, type BaseImageResponse, type Pageable } from '$lib/generated';
  import { onMount } from 'svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import PencilOutline from 'svelte-material-icons/PencilOutline.svelte';
  import Select from '$lib/components/Select.svelte';
  import { fetchBuildTools, fetchLanguages, getBaseImages, type BaseImageFilter } from '$lib/service/baseImageService';
  import ImageDelete from '$lib/components/pages/admin/image/ImageDelete.svelte';
  import ImageDetails from '$lib/components/pages/admin/image/ImageDetails.svelte';
  import ImageEditCreate from '$lib/components/pages/admin/image/ImageEditCreate.svelte';

  const pageable: Pageable = {
    page: 1,
    size: 20,
    sort: []
  };

  let editDialog: ImageEditCreate;
  let deleteDialog: ImageDelete;
  let detailsDialog: ImageDetails;

  let images: BaseImageResponse | undefined = undefined;

  let filterCriteria: BaseImageFilter = {};
  onMount(() => {
    loadData();
  });

  async function loadData() {
    images = await getBaseImages(filterCriteria, pageable);
  }

  function changePage({ detail: page }: CustomEvent<number>) {
    pageable.page = page;
    loadData();
  }

  let languages: string[] = [];
  let buildTools: string[] = [];

  onMount(() => {
    load();
    loadBuildTools();
  });

  async function load() {
    languages = await fetchLanguages();
  }

  async function loadBuildTools(language?: string | undefined) {
    buildTools = await fetchBuildTools(language || undefined, undefined);
  }

  $: filterCriteria, loadData();
  $: loadBuildTools(filterCriteria.language);
</script>

<div class="flex h-full flex-col gap-8 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="text-2xl font-bold">Base images</h2>
    <PrimaryButton href="/admin/image/create">+ Create base image</PrimaryButton>
  </div>
  <div class="grid grid-cols-3 gap-4">
    <Select
      id="image-type"
      name="image-type"
      bind:value={filterCriteria.type}
      options={Object.values(BaseImageType).map(s => ({ name: s, value: s }))}>
      Type
    </Select>
    <Select
      id="language-filter"
      name="language-filter"
      options={languages.map(s => ({ name: s, value: s }))}
      bind:value={filterCriteria.language}>
      Language
    </Select>

    <Select
      id="build-tool-filter"
      name="build-tool-filter"
      options={buildTools.map(s => ({ name: s, value: s }))}
      bind:value={filterCriteria.buildTool}>
      Build tool
    </Select>
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
          <th class="text-left">Language</th>
          <th class="text-left">Version</th>
          <th class="text-left">Build Tool</th>
          <th>Type</th>
          <th>Actions</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {#if images}
          {#each images.data as image}
            <tr>
              <td>
                {image.language}
              </td>
              <td>
                {image.version}
              </td>
              <td>
                <span class="text-nowrap">
                  {image.git?.buildTool || ''}{image.git?.buildToolVersion ? `:${image.git?.buildToolVersion}` : ''}
                </span>
              </td>
              <td class="text-center">
                {image.type}
              </td>

              <td class="text-center">
                <div class="flex justify-center gap-2">
                  <PrimaryLinkButton href="/admin/image/edit/{image.id}">
                    <PencilOutline class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Edit</span>
                  </PrimaryLinkButton>
                  <DangerLinkButton on:click={() => deleteDialog.open(image.id)}>
                    <TrashCanOutline class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Delete</span>
                  </DangerLinkButton>
                </div>
              </td>
              <td>
                <PrimaryLinkButton on:click={() => detailsDialog.open(image)}>
                  <InformationOutline class="inline-block" height="1.5em" width="1.5em" />
                </PrimaryLinkButton>
              </td>
            </tr>
          {/each}
        {/if}
      </tbody>
    </table>
    {#if images?.metadata}
      <div>
        <Pagination value={images.metadata.pagination} on:page={changePage} />
      </div>
    {/if}
  </div>
</div>

<ImageDelete bind:this={deleteDialog} on:save={loadData} />
<ImageDetails bind:this={detailsDialog} on:save={loadData} />
