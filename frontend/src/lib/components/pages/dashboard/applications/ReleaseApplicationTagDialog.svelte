<script lang="ts">
  import DangerButton from '$lib/components/buttons/DangerButton.svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import DefaultDialog from '$lib/components/dialogs/DefaultDialog.svelte';
  import ImageArgumentForm from '$lib/components/ImageArgumentForm.svelte';
  import ImageBaseForm from '$lib/components/ImageBaseForm.svelte';
  import ImageForm from '$lib/components/ImageForm.svelte';
  import { releaseImageTag } from '$lib/service/imageService';
  import { ImageType } from '$lib/types/baseImage';
  import type {
    ExeImageData,
    GitImageData,
    Image,
    ImageBaseData,
    ImageNameProp,
    PartialImage
  } from '$lib/types/baseImageRequest';
  import { createEventDispatcher } from 'svelte';

  const dispatch = createEventDispatcher<{
    save: void;
  }>();

  let dialog: DefaultDialog;
  let id: number | undefined;
  let image: Image | undefined;

  export const open = (nid: number, initial: PartialImage = {}) => {
    id = nid;
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
        ...(initial?.base ?? {}),
      },
      data: {
        name: '',
        version: '',
        ...(initial?.data ?? {}),
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

  export const closeDialog = () => {
    image = undefined;
    dialog.close();
  };

  const save = async () => {
    if (!image || !id) return;
    await releaseImageTag(id, image);
    dispatch("save");
    closeDialog();
  };
</script>

<DefaultDialog bind:this={dialog}>
  <svelte:fragment slot="title">Release application version</svelte:fragment>
  <svelte:fragment slot="content">
    <div class="min-h-[35rem] min-w-[50rem]">
      {#if image}
        <ImageForm bind:image />
        <ImageBaseForm bind:image />
        <ImageArgumentForm bind:image />
      {/if}
    </div>
  </svelte:fragment>
  <svelte:fragment slot="actions">
    <div>
      <DangerButton on:click={closeDialog}>Cancel</DangerButton>
    </div>
    <div>
      <PrimaryButton disabled={!image?.data.version} on:click={save}>Save</PrimaryButton>
    </div>
  </svelte:fragment>
</DefaultDialog>
