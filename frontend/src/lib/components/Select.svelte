<script lang="ts" context="module">
  export type SelectOption<T> = { value: T; name: string };
</script>

<script lang="ts">
  type T = $$Generic;

  export let id: string;
  export let name: string;

  export let value: T;
  // export let defaultValue: T | undefined = undefined;
  export let options: SelectOption<T>[];

  // $: defaultValue && (value ||= defaultValue);
</script>

<div class="flex flex-col gap-1">
  <label for={id} class="w-full text-xs opacity-45">
    <slot />
  </label>
  <select
    bind:value
    {id}
    {name}
    class="min-w-56 rounded border border-gray-200 bg-white px-4 py-1.5 shadow marker:ml-4">
    {#each options as option}
      <option value={option.value}>{option.name}</option>
    {/each}
  </select>
</div>
