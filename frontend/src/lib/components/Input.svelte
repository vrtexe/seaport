<script lang="ts">
  import type { HTMLInputTypeAttribute } from 'svelte/elements';

  type T = $$Generic;

  export let id: string;
  export let name: string;
  export let value: string;
  export let readonly: boolean = false;
  export let placeholder: string = '';
  export let inputClass: string = '';
  export let labelClass: string = '';
</script>

<div class="relative flex min-w-56 flex-col gap-1">
  <label for={id} class="text-xs opacity-45 {labelClass}">
    <slot />
  </label>

  <input
    bind:value
    {id}
    {name}
    {readonly}
    class="rounded border border-gray-200 px-4 py-1.5 shadow read-only:opacity-65 read-only:outline-none {inputClass}"
    type="text"
    {placeholder} />
  <slot name="other" />
</div>
