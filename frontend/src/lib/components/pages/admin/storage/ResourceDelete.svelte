<script lang="ts">
  import DangerButton from '$lib/components/buttons/DangerButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import { BASE_URL_WITHOUT_PATH } from '$lib/config';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher<{
    delete: void;
  }>();

  let dialog: DefaultDialog;
  let href: string | undefined;

  export const open = (nid: string | undefined) => {
    href = nid;
    dialog.open();
  };

  export const closeDialog = () => {
    href = undefined;
    dialog.close();
  };

  async function handleDelete() {
    await fetch(`${BASE_URL_WITHOUT_PATH}${href}`, { method: 'DELETE' });
    dispatch('delete');
    closeDialog();
  }
</script>

<DefaultDialog bind:this={dialog}>
  <svelte:fragment slot="title">Delete resource</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="grid w-[30rem] min-w-10 max-w-[50rem] gap-6 px-4 py-2">
      <p>Are you sure you want to delete this resource?</p>
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
