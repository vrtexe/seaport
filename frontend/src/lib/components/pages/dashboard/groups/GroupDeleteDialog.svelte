<script lang="ts">
  import DangerButton from '$lib/components/buttons/DangerButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import { deleteGroup } from '$lib/service/groupService';
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
    await deleteGroup(id).catch(e => console.log(e));
    dispatch('delete');
    closeDialog();
  }
</script>

<DefaultDialog bind:this={dialog}>
  <svelte:fragment slot="title">Delete Group</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="grid w-[30rem] min-w-10 max-w-[50rem] gap-6 px-4 py-2">
      <p>
        This will also delete all deployments associated with this group. Are you sure you want to delete this group?
      </p>
      <p>This action is not reversible</p>
    </div>
  </svelte:fragment>
  <svelte:fragment slot="actions">
    <div>
      <PrimaryButton on:click={closeDialog}>Canc el</PrimaryButton>
    </div>
    <div>
      <DangerButton on:click={handleDelete}>Confirm</DangerButton>
    </div>
  </svelte:fragment>
</DefaultDialog>
