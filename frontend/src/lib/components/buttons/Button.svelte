<script lang="ts">
  export let href: string | undefined = undefined;
  export let disabled = false;
  export { className as class };

  let className: string;
</script>

{#if !href}
  <button
    class="inline-block rounded-md border border-gray-300 px-4 py-2 font-bold hover:opacity-75 disabled:opacity-100 disabled:brightness-100 active:brightness-75 active:opacity-100 {className}"
    type="button"
    {disabled}
    on:click>
    <slot />
  </button>
{:else}
  <a
    {href}
    class="inline-block rounded-md border border-gray-300 px-4 py-2 font-bold hover:opacity-75 disabled:brightness-100 active:brightness-75 active:opacity-100 {className}"
    type="button"
    class:disabled
    on:click>
    <slot />
  </a>
{/if}
