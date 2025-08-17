<script lang="ts">
  import type { HTMLAttributeAnchorTarget } from 'svelte/elements';
  import Button from './Button.svelte';

  export let href: string | undefined = undefined;
  export let target: HTMLAttributeAnchorTarget | undefined = undefined;
  export let disabled: boolean = false;
</script>

<Button {href} {target} class="flex gap-2 border-none bg-transparent !px-2 !py-0 text-primary" bind:disabled on:click>
  <slot />
</Button>
