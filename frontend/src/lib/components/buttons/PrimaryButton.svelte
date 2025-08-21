<script lang="ts">
  import Button from './Button.svelte';

  export let href: string | undefined = undefined;
  export let classname = "";
  export let disabled = false;
</script>

<Button {href} {disabled} class="bg-primary text-white disabled:opacity-70 {classname}" on:click>
  <slot />
</Button>
