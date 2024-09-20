<script lang="ts">
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';

  type T = $$Generic;

  export let id: string;
  export let name: string;
  export let value: string;
  export let placeholder: string = '';
  export let description: string | undefined = undefined;
  export let labelClass: string = '';
</script>

<div class="grid grid-cols-[0.75fr_3fr_2em] items-center justify-center gap-2">
  <label for={id} class="flex items-center justify-end text-sm opacity-45 {labelClass}">
    <slot />
  </label>
  <input bind:value {id} {name} class="rounded border border-gray-200 px-4 py-1.5 shadow" type="text" {placeholder} />
  <span class="hint relative flex justify-center">
    {#if description}
      <InformationOutline height="1.5em" width="1.5em" class="flex items-center justify-center" />
      <div class="hidden">{description}</div>
    {/if}
  </span>
</div>

<style lang="postcss">
  .hint:hover div {
    display: inline-block;
    width: max-content;
    @apply absolute -top-7 right-0 select-none rounded bg-black px-4 py-0.5 text-white;
  }
</style>
