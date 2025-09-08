<script lang="ts">
  import DangerButton from '$lib/components/buttons/DangerButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import Notification, { NotificationType } from '$lib/components/pages/dashboard/deployment/Notification.svelte';
  import { ClientError } from '$lib/errors/ClientError';
  import type { BaseImage } from '$lib/generated';
  import { deleteImage } from '$lib/service/imageService';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher<{
    delete: void;
  }>();

  let dialog: DefaultDialog;
  let baseImage: BaseImage | undefined;

  let notification: Notification | undefined;

  export const open = (newBaseImage: BaseImage) => {
    baseImage = newBaseImage;
    dialog.open();
  };

  export const closeDialog = () => {
    baseImage = undefined;
    dialog.close();
  };
</script>

<DefaultDialog bind:this={dialog} contentClass="">
  <svelte:fragment slot="title">Image Arguments</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="grid min-w-10 max-w-[40rem] gap-6">
      {#if baseImage}
        <div class="grid border-collapse grid-cols-3 *:border-b *:px-8 *:py-2">
          <div class="text-xs font-bold">Name</div>
          <div class="text-xs font-bold">Type</div>
          <div class="text-xs font-bold">Description</div>
          {#each baseImage.arguments as arg}
            <div class="font-bold">
              {arg.name}
            </div>
            <div class="text-gray-500">
              {arg.type.toLowerCase()}
            </div>
            <div class="text-sm">
              {arg.description}
            </div>
          {/each}
        </div>
      {/if}
    </div>
  </svelte:fragment>
  <svelte:fragment slot="actions">
    <div>
      <PrimaryButton on:click={closeDialog}>Close</PrimaryButton>
    </div>
  </svelte:fragment>
</DefaultDialog>
