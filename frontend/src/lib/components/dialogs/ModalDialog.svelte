<script lang="ts">
  export let dialog: HTMLDialogElement;
  export let height: string;
</script>

<dialog bind:this={dialog} class="outline-none rounded-md overflow-hidden" style:height>
  <slot />
</dialog>
