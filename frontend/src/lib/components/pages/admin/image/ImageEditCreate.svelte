<script lang="ts">
  import DangerLinkButton from '$lib/components/buttons/DangerLinkButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import {
    BaseArgumentType,
    BaseImageType,
    type BaseImageArgument,
    type BaseImageDetails,
    type BaseImageRequest
  } from '$lib/generated';
  import { onMount } from 'svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import Plus from 'svelte-material-icons/Plus.svelte';
  import Select from '$lib/components/Select.svelte';
  import {
    createBaseImage,
    fetchBuildTools,
    fetchBuildToolVersions,
    fetchLanguages,
    fetchVersions,
    getBaseImage,
    updateBaseImage
  } from '$lib/service/baseImageService';
  import Input from '$lib/components/Input.svelte';
  import CodeEditor from '$lib/components/pages/admin/image/CodeEditor.svelte';
  import SecondaryButton from '$lib/components/buttons/SecondaryButton.svelte';
  import AccordionContent from '$lib/components/AccordionContent.svelte';

  export let id: number | undefined = undefined;

  let image: BaseImageDetails | undefined = undefined;

  let languages: string[] = [];
  let languageVersions: string[] = [];
  let buildTools: string[] = [];
  let buildToolsVersions: string[] = [];

  onMount(() => {
    loadData();
    load();
  });

  async function loadData() {
    if (id == undefined) return;
    image = await getBaseImage(id);

    type = image.base.type;
    language = image.base.language;
    buildTool = image.base.git?.buildTool;
    buildToolVersion = image.base.git?.buildToolVersion;
    languageVersionEnabled = Boolean(image.base.language);
    languageVersion = image.base.version;
    value = image.value;
    fileType = image.base.exe?.fileType || '';
    buildArguments = image.base.arguments;
  }

  async function load() {
    languages = await fetchLanguages();
  }

  async function loadLanguageVersions(language: string | undefined) {
    languageVersions = await fetchVersions(language);
  }

  async function loadBuildTools(language: string | undefined) {
    buildTools = await fetchBuildTools(language, undefined);
  }

  async function loadBuildToolVersions(buildTool: string | undefined) {
    buildToolsVersions = await fetchBuildToolVersions(buildTool);
  }

  let type: BaseImageType | undefined;
  let language: string | undefined;
  let fileType: string = '';
  let buildTool: string | undefined;
  let buildToolVersion: string | undefined;
  let languageVersionEnabled = false;
  let languageVersion: string | undefined;
  let value: string = '';
  let buildArguments: BaseImageArgument[] = [];

  function handleSave() {
    if (image) {
      updateBaseImage(image.base.id, {
        value,
        baseImage: {
          language: language!,
          version: languageVersion!
        },
        type: type!,
        arguments: buildArguments,
        baseImageId: image.baseImageId,
        ...getTypeSpecificProperties(type!)
      });
    } else {
      createBaseImage({
        value,
        baseImage: {
          language: language!,
          version: languageVersion!
        },
        type: type!,
        arguments: buildArguments,
        ...getTypeSpecificProperties(type!)
      });
    }
  }

  function getTypeSpecificProperties(type: BaseImageType): Partial<BaseImageRequest> {
    switch (type) {
      case BaseImageType.Git:
        return {
          git: {
            buildTool: buildTool!,
            buildToolVersion: buildToolVersion!
          }
        };
      case BaseImageType.Exe:
        return {
          exe: {
            fileType: ''
          }
        };
    }
  }

  $: isInvalid =
    !type ||
    !language ||
    (type === BaseImageType.Git && (!buildTool || !buildToolVersion)) ||
    (type === BaseImageType.Exe && !fileType) ||
    !value ||
    (languageVersionEnabled && !languageVersion);
  $: loadLanguageVersions(language);
  $: loadBuildTools(language);
  $: loadBuildToolVersions(buildTool);
</script>

<div class="flex h-full flex-col gap-8 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="text-2xl font-bold">
      <!-- {#if type === DialogType.Create}
        Create Deployment
      {:else if DialogType.Edit}
        Edit Deployment
      {/if} -->
    </h2>
    <div class="flex gap-2">
      <SecondaryButton href="/admin/image">Cancel</SecondaryButton>
      <PrimaryButton disabled={isInvalid} on:click={handleSave}>Save</PrimaryButton>
    </div>
  </div>
  <div class="flex flex-1 flex-col justify-between pb-4">
    <form class="grid gap-8">
      <fieldset class="grid grid-cols-2 gap-4">
        <legend class="pb-6 text-xs font-bold text-gray-400">General</legend>
        <Select
          id="image-type"
          name="image-type"
          bind:value={type}
          options={Object.values(BaseImageType).map(s => ({ name: s, value: s }))}>
          Type
        </Select>
        <div></div>

        <Select
          id="image-language"
          name="image-language"
          bind:value={language}
          createValue={s => s}
          options={languages.map(s => ({ name: s, value: s }))}>
          Language
        </Select>

        <div class="flex">
          <div class="flex-1 items-end">
            <Select
              id="image-language-version"
              name="image-language-version"
              bind:value={languageVersion}
              disabled={!languageVersionEnabled}
              createValue={s => s}
              options={languageVersions.map(s => ({ name: s, value: s }))}>
              Version
            </Select>
          </div>
          <label class="flex cursor-pointer items-end px-2 py-3.5 text-lg">
            <input type="checkbox" class="cursor-pointer accent-primary" bind:checked={languageVersionEnabled} />
          </label>
        </div>
      </fieldset>

      <fieldset class="grid grid-cols-2 gap-4">
        <legend class="pb-6 text-xs font-bold text-gray-400">Git properties</legend>
        <Select
          id="image-build-tool"
          name="image-build-tool"
          bind:value={buildTool}
          createValue={s => s}
          disabled={type !== BaseImageType.Git}
          options={buildTools.map(s => ({ name: s, value: s }))}>
          Build tool
        </Select>

        <Select
          id="image-build-tool-version"
          name="image-build-tool-version"
          bind:value={buildToolVersion}
          createValue={s => s}
          disabled={type !== BaseImageType.Git}
          options={buildToolsVersions.map(s => ({ name: s, value: s }))}>
          Version
        </Select>
      </fieldset>

      <fieldset class="grid gap-4">
        <legend class="pb-6 text-xs font-bold text-gray-400">Exe properties</legend>
        <Input id="image-file-type" name="image-file-type" bind:value={fileType} disabled={type !== BaseImageType.Exe}>
          File type
        </Input>
      </fieldset>
      <fieldset class="grid grid-cols-[1fr_1fr_1fr_auto] gap-4">
        <legend class="flex w-full items-center justify-between pb-6 text-xs font-bold text-gray-400">
          <span>Build arguments</span>
          <PrimaryButton
            classname="rounded-full px-[0.75rem]"
            on:click={() =>
              (buildArguments = [...buildArguments, { name: '', description: '', stage: 'BUILD', type: 'STRING' }])}>
            <Plus class="inline-flex" size="0.75rem" />
          </PrimaryButton>
        </legend>

        {#each buildArguments as a, i}
          <Input id="argument-name" name="argument-name" bind:value={a.name}>Name</Input>
          <Input id="argument-description" name="argument-description" bind:value={a.description}>Description</Input>
          <Select
            id="argument-type"
            name="argument-type"
            bind:value={a.type}
            options={Object.values(BaseArgumentType).map(s => ({ name: s, value: s }))}>Type</Select>

          <div class="flex items-end py-2.5">
            <DangerLinkButton on:click={() => (buildArguments = buildArguments.filter(s => s !== a))}>
              <TrashCanOutline size="1.5rem" class="inline-flex" />
            </DangerLinkButton>
          </div>
        {/each}
      </fieldset>
      <fieldset class="grid gap-4">
        <legend class="flex w-full items-center justify-between pb-6 text-xs font-bold text-gray-400">
          <div>Script</div>
        </legend>
        <div class="w-full">
          <CodeEditor class="min-h-[50rem]" bind:value language="dockerfile"></CodeEditor>
        </div>
      </fieldset>
    </form>
  </div>
</div>

<!-- <SimpleCodeEditor bind:value>as</SimpleCodeEditor> -->
