<script lang="ts">
  import Button from './Button.svelte';

  export let href: string | undefined = undefined;
  export let disabled = false;
</script>

<Button
  {href}
  {disabled}
  class="border-primary bg-white text-primary hover:text-secondary hover:border-secondary disabled:opacity-70"
  on:click>
  <slot />
</Button>
