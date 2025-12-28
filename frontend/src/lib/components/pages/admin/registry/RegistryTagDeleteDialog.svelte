<script lang="ts">
  import DangerButton from '$lib/components/buttons/DangerButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import Notification, { NotificationType } from '$lib/components/pages/dashboard/deployment/Notification.svelte';
  import { ClientError } from '$lib/errors/ClientError';
  import { deleteRegistryImageTag } from '$lib/service/registryService';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher<{ delete: void }>();

  let dialog: DefaultDialog;
  let id: number | undefined;
  let notification: Notification | undefined;

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
    await deleteRegistryImageTag(id).catch(e => {
      if (e instanceof ClientError) {
        notification?.open({ type: NotificationType.Error, text: e.message });
      }
    });
    dispatch('delete');
    closeDialog();
  }
</script>

<DefaultDialog bind:this={dialog}>
  <svelte:fragment slot="title">Delete registry tag</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="grid w-[30rem] min-w-10 max-w-[50rem] gap-6 px-4 py-2">
      <p>Are you sure you want to delete this tag?</p>
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

<Notification bind:this={notification} />
