<script>
  import Button from './Button.svelte';
</script>

<Button class="bg-danger text-white" on:click>
  <slot />
</Button>