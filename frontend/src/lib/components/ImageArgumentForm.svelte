<script lang="ts">
  import FileInput from '$lib/components/FileInput.svelte';
  import InputHorizontal from '$lib/components/InputHorizontal.svelte';
  import { fetchImageArgs } from '$lib/service/baseImageService';
  import { BuildArgStage, BuildArgType, ImageType, type BuildArg } from '$lib/types/baseImage';
  import type { Image } from '$lib/types/baseImageRequest';

  export let image: Image;

  let imageArgs: BuildArg[] = [];
  let buildArgs: BuildArg[] = [];

  let timeout: ReturnType<typeof setTimeout>;

  function handleImageUpdate(buildArgs: BuildArg[]) {
    clearTimeout(timeout);
    timeout = setTimeout(() => updateImage(buildArgs), 2000);
  }

  function updateImage(buildArgs: BuildArg[]) {
    image.buildArgs = toImageArguments(buildArgs);
    if (image.type === ImageType.Exe) {
      image.file = findFile(buildArgs);
    }
  }

  function toImageArguments(buildArgs: BuildArg[]): Record<string, string> {
    return buildArgs.reduce(
      (acc, arg) =>
        arg.type === BuildArgType.String
          ? {
              ...acc,
              [arg.name]: arg.value
            }
          : acc,
      {} as Record<string, string>
    );
  }

  function findFile(buildArgs: BuildArg[]) {
    return buildArgs.find(arg => arg.type === BuildArgType.File)?.value?.[0];
  }

  async function updateImageArgs(
    type: ImageType,
    language: string | undefined,
    languageVersion: string | undefined,
    buildTool: string | undefined,
    buildToolVersion: string | undefined
  ) {
    buildArgs = [];
    imageArgs = await fetchImageArgs(type, language, languageVersion, buildTool, buildToolVersion);
  }

  let imageType: ImageType = image.type;
  let language: string = '';
  let languageVersion: string = '';
  let buildTool: string | undefined = undefined;
  let buildToolVersion: string | undefined = undefined;

  $: if (imageType !== image.type) {
    imageType = image.type;
  }

  $: if (language !== image.base.language) {
    language = image.base.language;
  }

  $: if (languageVersion !== image.base.version) {
    languageVersion = image.base.version;
  }

  $: if (image.type !== ImageType.Git) {
    buildTool = undefined;
  } else if (buildTool !== image.buildTool) {
    buildTool = image.buildTool;
  }

  $: if (image.type !== ImageType.Git) {
    buildToolVersion = undefined;
  } else if (buildToolVersion !== image.version) {
    buildToolVersion = image.version;
  }

  $: buildArgs = imageArgs
    .filter(s => s.stage === BuildArgStage.Build)
    .toSorted((left, right) => right.type.localeCompare(left.type));

  $: handleImageUpdate(buildArgs);
  $: updateImageArgs(imageType, language, languageVersion, buildTool, buildToolVersion);
</script>

<div class="py-4">
  {#if buildArgs?.length}
    <div class="grid grid-cols-1 gap-4">
      {#each buildArgs as buildArg}
        {#if buildArg.type === BuildArgType.String}
          <InputHorizontal
            id={buildArg.name}
            name={buildArg.name}
            bind:value={buildArg.value}
            description={buildArg.description}>
            {buildArg.name}
          </InputHorizontal>
        {:else if buildArg.type === BuildArgType.File}
          <FileInput id={buildArg.name} name={buildArg.name} bind:value={buildArg.value}>
            <div class="flex items-center justify-between px-2 py-1">
              <span class="text-sm font-bold">{buildArg.name}</span>
              {#if buildArg.description}
                <span
                  class="inline-block w-max select-none justify-center rounded bg-black px-4 py-0.5 font-bold text-white">
                  <div>{buildArg.description}</div>
                </span>
              {/if}
            </div>
          </FileInput>
        {/if}
      {/each}
    </div>
  {:else}
    <p class="text-sm text-gray-600">No content, please fill in the base properties</p>
  {/if}
</div>
