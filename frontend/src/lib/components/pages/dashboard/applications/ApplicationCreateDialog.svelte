<script lang="ts">
  import DangerButton from '$lib/components/buttons/DangerButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import Input from '$lib/components/Input.svelte';
  import { createImage, type ImageCreateRequest } from '$lib/service/imageService';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher<{
    save: void;
  }>();

  let data: ImageCreateRequest = {
    name: ''
  };

  let dialog: DefaultDialog;

  export const open = () => {
    data = {
      name: ''
    };

    dialog.open();
  };

  export const closeDialog = () => {
    dialog.close();
  };

  const save = async () => {
    if (!data.name) return;
    createImage(data)
      .then(() => {
        dispatch('save');
        closeDialog();
      })
      .catch(e => console.error(e));
  };
</script>

<DefaultDialog bind:this={dialog}>
  <svelte:fragment slot="title">Create Application</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="w-[30rem] min-w-10 max-w-[50rem] px-4 py-8">
      <Input id="application-name" name="application-name" bind:value={data.name}>Name</Input>
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

