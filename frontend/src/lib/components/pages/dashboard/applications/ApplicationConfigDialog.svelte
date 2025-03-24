<script lang="ts">
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import ImageArgumentForm from '$lib/components/ImageArgumentForm.svelte';
  import { ImageType } from '$lib/types/baseImage';
  import type {
    ExeImageData,
    GitImageData,
    Image,
    ImageBaseData,
    ImageNameProp,
    PartialImage
  } from '$lib/types/baseImageRequest';

  let dialog: DefaultDialog;
  let image: Image | undefined;

  export const closeDialog = () => {
    dialog.close();
    image = undefined;
  };

  export const open = (initial: PartialImage = {}) => {
    image = defaultImageWith(initial);
    dialog.open();
  };

  const defaultImageWith = (initial: PartialImage = {}): Image => {
    initial.type ||= ImageType.Git;

    const baseImage: ImageNameProp & ImageBaseData = {
      uid: crypto.randomUUID(),
      name: 'Default',
      base: {
        language: '',
        version: '',
        ...(initial?.base ?? {})
      },
      data: {
        name: '',
        version: '',
        ...(initial?.data ?? {})
      },
      buildArgs: {
        ...(initial?.buildArgs ?? {})
      }
    };

    switch (initial.type) {
      case ImageType.Git:
        return {
          ...baseImage,
          ...(<GitImageData>{
            type: ImageType.Git,
            buildTool: initial.buildTool,
            version: initial.version
          })
        };
      case ImageType.Exe:
        return {
          ...baseImage,
          ...(<ExeImageData>{
            type: ImageType.Exe,
            file: initial.file
          })
        };
    }
  };
</script>

<DefaultDialog bind:this={dialog}>
  <svelte:fragment slot="title">Application config</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="min-w-[60rem] max-w-[90rem]">
      {#if image}
        <ImageArgumentForm readonly {image} />
      {/if}
    </div>
  </svelte:fragment>
  <svelte:fragment slot="actions">
    <div>
      <PrimaryButton on:click={closeDialog}>Close</PrimaryButton>
    </div>
  </svelte:fragment>
</DefaultDialog>
