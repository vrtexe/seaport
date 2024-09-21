<script lang="ts">
  import Input from '$lib/components/Input.svelte';
  import Plus from 'svelte-material-icons/Plus.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import type { DeploymentDto, Image } from '$lib/types/baseImageRequest';
  import { fetchImageArgsFromDto } from '$lib/service/baseImageService';
  import { BuildArgStage, ImageType, type BuildArg } from '$lib/types/baseImage';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';

  const cachedImageArgs: Record<string, BuildArg[]> = {};
  export let deployment: DeploymentDto;
  export let images: Record<string, Image>;

  let image: Image | undefined;
  let applicationProperties: ApplicationProperty[] = [];

  function addProperty() {
    applicationProperties = [
      ...applicationProperties,
      {
        name: '',
        value: '',

        custom: true
      }
    ];
  }

  function toImageArgId(image: Image) {
    return `${asId(image.type)}-${asId(image.base.language)}-${asId(image.base.version)}${toGitImageArgId(image)}`;
  }

  function toGitImageArgId(image: Image) {
    return image.type === ImageType.Git ? `-${asId(image.buildTool)}-${asId(image.version)}` : '';
  }

  function asId(arg: string) {
    return arg ? arg.replaceAll(/\s+/g, ' ').toLowerCase() : '';
  }

  async function loadImageArgs(image: Image) {
    const imageArgId = toImageArgId(image);

    const imageArguments = cachedImageArgs[imageArgId]
      ? cachedImageArgs[imageArgId]
      : await fetchImageArgsFromDto(image);
    cachedImageArgs[imageArgId] = imageArguments;

    const runtimeImageArguments = imageArguments.filter(argument => argument.stage === BuildArgStage.Runtime);
    const runtimeImageArgumentNames = runtimeImageArguments.map(s => s.name);

    const props = {
      ...runtimeImageArguments
        .map(s => ({
          name: s.name,
          value: '',
          description: s.description,
          custom: false
        }))
        .reduce(groupByName, {}),
      ...applicationProperties
        .filter(property => (runtimeImageArgumentNames.includes(property.name) && !property.custom) || property.custom)
        .reduce(groupByName, {})
    };
    applicationProperties = Object.values(props);
  }

  function groupByName(acc: Record<string, ApplicationProperty>, prop: ApplicationProperty) {
    return {
      ...acc,
      [prop.name]: prop
    };
  }

  let timeout: ReturnType<typeof setTimeout>;
  function handleDeploymentProperties(properties: ApplicationProperty[]) {
    clearTimeout(timeout);
    timeout = setTimeout(() => updateDeploymentProperties(properties), 1000);
  }

  function updateDeploymentProperties(properties: ApplicationProperty[]) {
    environment.values = properties
      .filter(prop => prop.name && prop.value)
      .reduce(
        (acc, prop) => ({
          ...acc,
          [prop.name]: prop.value
        }),
        {} as Record<string, string>
      );
  }

  type ApplicationProperty = {
    name: string;
    value: string;
    custom: boolean;
    description?: string;
  };

  let deploymentImageUid: string | undefined;

  $: environment = deployment.environment;
  $: if (deploymentImageUid !== deployment.image) {
    deploymentImageUid = deployment.image;
  }

  $: image = deploymentImageUid ? images[deploymentImageUid] : undefined;
  $: image && loadImageArgs(image);
  $: handleDeploymentProperties(applicationProperties);
</script>

<div class="py-4">
  {#if applicationProperties?.length}
    <div class="grid grid-cols-1 gap-4">
      {#each applicationProperties as property, propertyIndex}
        <div class="grid grid-cols-[1fr_2fr_2em] gap-4">
          <Input id="prop-name" name="prop-name" readonly={!property.custom} bind:value={property.name}>Name</Input>
          <Input id="prop-val" name="prop-val" bind:value={property.value}>Value</Input>
          <div class="relative flex items-end border border-transparent py-1.5">
            <button
              type="button"
              class:hidden={!property.custom}
              on:click={() => {
                applicationProperties = applicationProperties.filter((_, i) => propertyIndex !== i);
              }}>
              <TrashCanOutline height="1.5em" width="1.5em" />
            </button>
            {#if !property.custom}
              <div>
                <span class="hint relative flex w-full justify-center">
                  {#if property.description}
                    <InformationOutline height="1.5em" width="1.5em" class="flex items-center justify-center" />
                    <div class="invisible whitespace-nowrap">{property.description}</div>
                  {/if}
                </span>
              </div>
            {/if}
          </div>
        </div>
      {/each}
    </div>
    <div class="flex justify-end py-4">
      <button class="primary-circle" type="button" on:click={addProperty}>
        <Plus height="1.5em" width="1.5em" />
      </button>
    </div>
  {:else}
    <p class="flex justify-between text-sm text-gray-600">
      <span>No properties defined yet</span>
      <button class="primary-circle" type="button" on:click={addProperty}>
        <Plus height="1.5em" width="1.5em" />
      </button>
    </p>
  {/if}
</div>

<style lang="postcss">
  .hint:hover div {
    width: max-content;

    @apply visible absolute -top-7 right-0 inline-block select-none rounded bg-black px-4 py-0.5 text-white;
  }
</style>
