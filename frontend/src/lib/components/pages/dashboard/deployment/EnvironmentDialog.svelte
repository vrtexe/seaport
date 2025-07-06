<script lang="ts">
  import DangerButton from '$lib/components/buttons/DangerButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import Environment from '$lib/components/pages/dashboard/deployment/Environment.svelte';
  import { type Environment as EnvironmentData } from '$lib/components/pages/dashboard/deployment/types';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher<{
    save: EnvironmentData;
  }>();

  let data: EnvironmentData = {
    values: []
  };

  let dialog: DefaultDialog;

  export const open = (initial?: EnvironmentData) => {
    data = deepCopy(initial) ?? {
      values: []
    };

    dialog.open();
  };

  export const closeDialog = () => {
    dialog.close();
  };

  const save = async () => {
    dispatch('save', data);
    dialog.close();
  };

  const deepCopy = <T,>(value?: T): T | undefined => {
    if (!value) return undefined;
    return JSON.parse(JSON.stringify(value));
  };
</script>

<DefaultDialog height="80%" bind:this={dialog}>
  <svelte:fragment slot="title">Environment</svelte:fragment>
  <svelte:fragment slot="content">
    <div class=" min-w-[60rem] max-w-[90rem]">
      <Environment bind:data />
    </div>
  </svelte:fragment>
  <svelte:fragment slot="actions">
    <div>
      <DangerButton on:click={closeDialog}>Cancel</DangerButton>
    </div>
    <div>
      <PrimaryButton disabled={!data.values.every(p => p.name && p.value)} on:click={save}>Save</PrimaryButton>
    </div>
  </svelte:fragment>
</DefaultDialog>
