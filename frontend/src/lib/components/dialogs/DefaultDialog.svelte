<script lang="ts">
  import ModalDialog from '$lib/components/dialogs/ModalDialog.svelte';
  import Close from 'svelte-material-icons/Close.svelte';

  export let height = '';
  export let contentClass: string = 'px-4 py-2';

  let dialog: HTMLDialogElement;

  export const open = () => {
    dialog.showModal();
  };

  export const close = () => {
    dialog.close();
  };
</script>

<ModalDialog {height} bind:dialog>
  <div class="grid h-full grid-rows-[auto_1fr_auto]">
    <div class="flex select-none justify-between border-b bg-primary px-3 py-2 text-white shadow-sm">
      <span class="text-lg font-semibold">
        <slot name="title" />
      </span>
      <span>
        <button class="h-full focus:outline-none" on:click={close}>
          <Close size="1.5em" />
        </button>
      </span>
    </div>
    <div class="overflow-auto {contentClass}">
      <slot name="content" />
    </div>
    <div class="flex justify-end gap-2 border-t px-3 py-2">
      <slot name="actions" />
    </div>
  </div>
</ModalDialog>
