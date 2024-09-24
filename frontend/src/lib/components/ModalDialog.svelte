<script lang="ts">
  export let dialog: HTMLDialogElement;
</script>

<dialog bind:this={dialog} class="outline-none rounded-md">
  <slot />
</dialog>
