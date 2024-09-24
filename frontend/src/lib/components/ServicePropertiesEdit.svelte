<script lang="ts" context="module">
  export type ApplicationProperty = {
    name: string;
    value: string;
    custom: boolean;
    description?: string;
  };
</script>

<script lang="ts">
  import Input from '$lib/components/Input.svelte';
  import Plus from 'svelte-material-icons/Plus.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';

  export let applicationProperties: ApplicationProperty[] = [];

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
</script>

<div class="py-4">
  {#if applicationProperties?.length}
    <div class="flex justify-end py-4">
      <button class="primary-circle" type="button" on:click={addProperty}>
        <Plus height="1.5em" width="1.5em" />
      </button>
    </div>
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
