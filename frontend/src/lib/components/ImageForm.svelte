<script lang="ts">
  import Select from '$lib/components/Select.svelte';
  import {
    fetchBuildTools,
    fetchBuildToolVersions,
    fetchLanguages,
    fetchVersions
  } from '$lib/service/baseImageService';
  import { ImageType } from '$lib/types/baseImage';
  import type { Image } from '$lib/types/baseImageRequest';
  import { onMount } from 'svelte';

  export let image: Image;

  let languages: string[] = [];
  let languageVersions: string[] = [];
  let buildTools: string[] = [];
  let buildToolVersions: string[] = [];

  onMount(() => {
    updateLanguages();
  });

  async function updateLanguages() {
    languages = await fetchLanguages();
    image.base.language = languages[0];
  }

  async function updateVersions(language: string | undefined) {
    languageVersions = await fetchVersions(language);
    image.base.version = languageVersions[0];
  }

  async function updateBuildTools(language: string | undefined, version: string | undefined) {
    if (image.type !== ImageType.Git) {
      return;
    }

    buildTools = await fetchBuildTools(language, version);
    image.buildTool = buildTools[0];
  }

  async function updateBuildToolVersions(buildTool: string | undefined) {
    if (image.type !== ImageType.Git) {
      return;
    }

    buildToolVersions = await fetchBuildToolVersions(buildTool);
    image.version = buildToolVersions[0];
  }

  $: language = image.base.language;
  $: languageVersion = image.base.version;
  $: buildTool = image.type === ImageType.Git ? image.buildTool : undefined;

  $: updateVersions(language);
  $: updateBuildTools(language, languageVersion);
  $: updateBuildToolVersions(buildTool);
</script>

<div class="flex flex-col gap-6 py-4">
  <div class="flex">
    <Select
      id="type"
      name="type"
      bind:value={image.type}
      options={Object.values(ImageType).map(it => ({ name: it, value: it }))}>
      Type
    </Select>
  </div>
  <div class="flex gap-4">
    <Select
      id="language"
      name="language"
      bind:value={image.base.language}
      options={languages.map(it => ({ name: it, value: it }))}>
      Language
    </Select>

    <Select
      id="language-version"
      name="language-version"
      bind:value={image.base.version}
      options={languageVersions.map(it => ({ name: it, value: it }))}>
      Language version
    </Select>
  </div>

  {#if image.type === ImageType.Git}
    <div class="flex gap-4">
      <Select
        id="build-tool"
        name="build-tool"
        bind:value={image.buildTool}
        options={buildTools.map(bt => ({ name: bt, value: bt }))}>
        Build tool
      </Select>

      <Select
        id="build-tool"
        name="build-tool"
        bind:value={image.version}
        options={buildToolVersions.map(btv => ({ name: btv, value: btv }))}>
        Build tool version
      </Select>
    </div>
  {/if}
</div>
