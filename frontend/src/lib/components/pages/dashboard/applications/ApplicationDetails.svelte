<script lang="ts">
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import { BaseImageType, type ImageDetails, type ImageTagDetails } from '$lib/generated';
  import Plus from 'svelte-material-icons/Plus.svelte';
  import ArrowLeft from 'svelte-material-icons/ArrowLeft.svelte';
  import TextBox from 'svelte-material-icons/TextBox.svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import { format } from 'date-fns';
  import ReleaseApplicationTagDialog from '$lib/components/pages/dashboard/applications/ReleaseApplicationTagDialog.svelte';
  import ApplicationLogsDialog from '$lib/components/pages/dashboard/applications/ApplicationLogsDialog.svelte';
  import ApplicationBuildStatus from '$lib/components/pages/dashboard/applications/ApplicationBuildStatus.svelte';
  import { createEventDispatcher } from 'svelte';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import ApplicationConfigDialog from '$lib/components/pages/dashboard/applications/ApplicationConfigDialog.svelte';
  import type { PartialImage } from '$lib/types/baseImageRequest';

  const imageTypeLabel: Record<BaseImageType, string> = {
    GIT: 'Git',
    EXE: 'Executable'
  };

  const dispatch = createEventDispatcher<{
    release: void;
  }>();

  export let image: ImageDetails;

  let releaseDialog: ReleaseApplicationTagDialog;
  let configDialog: ApplicationConfigDialog;
  let logDialog: ApplicationLogsDialog;

  function partialImageProps(tag: ImageTagDetails): PartialImage {
    return {
      buildTool: tag.base.git?.buildTool,
      version: tag.base.git?.buildToolVersion
    };
  }
</script>

<div class="flex h-full flex-col gap-16 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="flex text-2xl font-bold">
      <PrimaryLinkButton on:click={() => window.history.back()}>
        <ArrowLeft class="inline-block" height="1.5em" width="1.5em" />
        {image?.name ?? ''}
      </PrimaryLinkButton>
    </h2>
    <PrimaryButton on:click={() => releaseDialog.open(image.id, { data: { name: image.name } })}>
      + New Release
    </PrimaryButton>
  </div>
  <div class="flex flex-1 flex-col justify-between">
    <table>
      <colgroup>
        <col class="w-0" />
        <col />
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
      </colgroup>
      <thead>
        <tr>
          <th>Status</th>
          <th class="text-left">Version</th>
          <th class="text-left">Released</th>
          <th>Type</th>
          <th>Actions</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {#if image}
          {#each image.tags as tag}
            <tr>
              <td>
                <ApplicationBuildStatus bind:tag />
              </td>
              <td>{tag.version}</td>
              <td class="text-nowrap">{format(tag.created, 'dd.MM.yyyy HH:mm') ?? ''}</td>
              <td class="text-center">{imageTypeLabel[tag.base.type]}</td>
              <td class="text-center">
                <div class="flex justify-center gap-2">
                  <PrimaryLinkButton
                    on:click={() =>
                      releaseDialog.open(image.id, {
                        type: tag.base.type,
                        buildArgs: tag.arguments,
                        data: { name: image.name },
                        base: { language: tag.base.language, version: tag.base.version },
                        ...partialImageProps(tag)
                      })}>
                    <Plus class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Release</span>
                  </PrimaryLinkButton>
                  <PrimaryLinkButton on:click={() => logDialog.open(tag)}>
                    <TextBox class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Logs</span>
                  </PrimaryLinkButton>
                </div>
              </td>
              <td>
                <PrimaryLinkButton
                  on:click={() =>
                    configDialog.open({
                      type: tag.base.type,
                      buildArgs: tag.arguments,
                      data: { name: image.name },
                      base: { language: tag.base.language, version: tag.base.version },
                      ...partialImageProps(tag)
                    })}>
                  <InformationOutline class="inline-block" height="1.75em" width="1.75em" />
                </PrimaryLinkButton>
              </td>
            </tr>
          {/each}
        {/if}
      </tbody>
    </table>
  </div>
</div>

<ReleaseApplicationTagDialog bind:this={releaseDialog} on:save={() => dispatch('release')} />
<ApplicationLogsDialog bind:this={logDialog} />
<ApplicationConfigDialog bind:this={configDialog} />
