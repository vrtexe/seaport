<script lang="ts">
  import Plus from 'svelte-material-icons/Plus.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import Input from '$lib/components/Input.svelte';
  import type { Environment } from '$lib/components/pages/dashboard/deployment/types';

  export let data: Environment;

  export let readonly: boolean = false;

  const addProperty = () => {
    data.values.push({
      name: '',
      value: ''
    });
    data = data;
  };

  const removePropertyAt = (index: number) => {
    data.values.splice(index, 1);
    data = data;
  };
</script>

<div>
  {#if !readonly}
    <div class="flex w-full justify-between pb-3 text-sm text-gray-400">
      <span class="flex items-center">Properties</span>
      <button
        class="rounded-lg border-2 border-primary bg-white p-0.5 text-primary opacity-65 brightness-100 hover:opacity-100 active:brightness-90"
        type="button"
        on:click={addProperty}>
        <Plus size="1.5em" />
      </button>
    </div>
  {/if}

  <div>
    {#if data.values.length}
      <div class="grid grid-cols-1 gap-4">
        {#each data.values as property, propertyIndex}
          <div class="grid grid-cols-[1fr_2fr_2em] gap-4">
            <Input
              id="environment-property-name-{propertyIndex}"
              name="environment-property-name={propertyIndex}"
              bind:value={property.name}
              {readonly}
              required>Name</Input>
            <Input
              id="environment-property-name-value-{propertyIndex}"
              name="environment-property-name-value-{propertyIndex}"
              bind:value={property.value}
              {readonly}
              required>Value</Input>
            {#if !readonly}
              <!-- content here -->
              <div class="relative flex items-end border border-transparent py-1.5">
                <button
                  type="button"
                  class="opacity-65 hover:opacity-100"
                  on:click={() => removePropertyAt(propertyIndex)}>
                  <TrashCanOutline size="1.5em" />
                </button>
              </div>
            {/if}
          </div>
        {/each}
      </div>
    {:else}
      <p class="flex justify-between px-4 text-sm text-gray-600">
        <span>No environment properties</span>
      </p>
    {/if}
  </div>
</div>
