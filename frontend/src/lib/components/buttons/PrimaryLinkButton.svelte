<script lang="ts">
  import Button from './Button.svelte';

  export let href: string | undefined = undefined;
</script>

<Button {href} class="flex gap-2 border-none bg-transparent !px-2 !py-0 text-primary" on:click>
  <slot />
</Button>
