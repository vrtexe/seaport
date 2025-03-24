<script lang="ts">
  import DangerButton from '$lib/components/buttons/DangerButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import Input from '$lib/components/Input.svelte';
  import type { GroupUpdateRequest } from '$lib/generated';
  import { updateGroup } from '$lib/service/groupService';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher<{
    save: void;
  }>();

  let id: number | undefined;
  let data: GroupUpdateRequest = {
    name: ''
  };

  let dialog: DefaultDialog;

  export const open = (oid: number, initial: Partial<GroupUpdateRequest> = {}) => {
    id = oid;
    data = {
      name: '',
      ...initial
    };

    dialog.open();
  };

  export const closeDialog = () => {
    id = undefined;
    dialog.close();
  };

  const save = async () => {
    if (!data.name || !id) return;
    updateGroup(id, data)
      .then(() => {
        dispatch('save');
        closeDialog();
      })
      .catch(e => console.error(e));
  };
</script>

<DefaultDialog bind:this={dialog}>
  <svelte:fragment slot="title">Edit Group</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="w-[30rem] min-w-10 max-w-[50rem] px-4 py-8">
      <Input id="application-name" name="application-name" bind:value={data.name} required>Name</Input>
    </div>
  </svelte:fragment>
  <svelte:fragment slot="actions">
    <div>
      <DangerButton on:click={closeDialog}>Cancel</DangerButton>
    </div>
    <div>
      <PrimaryButton disabled={!data.name} on:click={save}>Save</PrimaryButton>
    </div>
  </svelte:fragment>
</DefaultDialog>
