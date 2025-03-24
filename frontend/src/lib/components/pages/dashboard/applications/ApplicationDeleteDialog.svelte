<script lang="ts">
  import DangerButton from '$lib/components/buttons/DangerButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import { deleteImage } from '$lib/service/imageService';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher<{
    delete: void;
  }>();

  let dialog: DefaultDialog;
  let id: number | undefined;

  export const open = (nid: number) => {
    id = nid;
    dialog.open();
  };

  export const closeDialog = () => {
    id = undefined;
    dialog.close();
  };

  async function handleDelete() {
    if (id === undefined) return;
    await deleteImage(id);
    dispatch('delete');
    closeDialog();
  }
</script>

<DefaultDialog bind:this={dialog}>
  <svelte:fragment slot="title">Delete application</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="w-[30rem] min-w-10 max-w-[50rem] px-4 py-2 grid gap-6">
      <p>Are you sure you want to delete this application?</p>
      <p>This action is not reversible</p>
    </div>
  </svelte:fragment>
  <svelte:fragment slot="actions">
    <div>
      <PrimaryButton on:click={closeDialog}>Cancel</PrimaryButton>
    </div>
    <div>
      <DangerButton on:click={handleDelete}>Confirm</DangerButton>
    </div>
  </svelte:fragment>
</DefaultDialog>
