<script lang="ts">
  import Button from './Button.svelte';
</script>

<Button class="text-danger flex gap-2 border-none bg-transparent !px-2 !py-0" on:click>
  <slot />
</Button>
