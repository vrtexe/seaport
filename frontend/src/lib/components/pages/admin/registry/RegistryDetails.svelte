<script lang="ts">
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import DangerLinkButton from '$lib/components/buttons/DangerLinkButton.svelte';
  import ArrowLeft from 'svelte-material-icons/ArrowLeft.svelte';
  import TextBox from 'svelte-material-icons/TextBox.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';
  import { BaseImageType, type ImageDetails } from '$lib/generated';
  import { format } from 'date-fns';
  import RegistryLogsDialog from '$lib/components/pages/admin/registry/RegistryLogsDialog.svelte';
  import RegistryTagDeleteDialog from '$lib/components/pages/admin/registry/RegistryTagDeleteDialog.svelte';
  import { createEventDispatcher } from 'svelte';

  const imageTypeLabel: Record<BaseImageType, string> = {
    GIT: 'Git',
    EXE: 'Executable'
  };

  export let image: ImageDetails;

  let logDialog: RegistryLogsDialog;
  let deleteTagDialog: RegistryTagDeleteDialog;
  const dispatch = createEventDispatcher<{ refresh: void }>();

  function formatUser(user?: { username?: string; firstName?: string; lastName?: string; uid: string }) {
    if (!user) return '';
    if (user.username) return user.username;
    const name = [user.firstName, user.lastName].filter(Boolean).join(' ').trim();
    return name || user.uid;
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
    <div class="py-4 text-xs font-bold text-gray-400">User: {formatUser(image?.user)}</div>
  </div>
  <div class="flex flex-1 flex-col justify-between">
    <table>
      <colgroup>
        <col class="w-0" />
        <col />
        <col class="w-0" />
        <col class="w-0" />
        <col class="w-0" />
      </colgroup>
      <thead>
        <tr>
          <th class="text-left">Version</th>
          <th class="text-left">Released</th>
          <th>Type</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {#if image}
          {#each image.tags as tag}
            <tr>
              <td>{tag.version}</td>
              <td class="text-nowrap">{format(tag.created, 'dd.MM.yyyy HH:mm') ?? ''}</td>
              <td class="text-center">{imageTypeLabel[tag.base.type]}</td>
              <td class="text-center">
                <div class="flex justify-center gap-2">
                  <PrimaryLinkButton on:click={() => logDialog.open(tag.id)}>
                    <TextBox class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Logs</span>
                  </PrimaryLinkButton>
                  <DangerLinkButton on:click={() => deleteTagDialog.open(tag.id)}>
                    <TrashCanOutline class="inline-block" height="1.5em" width="1.5em" />
                    <span class="hidden lg:inline-block">Delete</span>
                  </DangerLinkButton>
                </div>
              </td>
            </tr>
          {/each}
        {/if}
      </tbody>
    </table>
  </div>
</div>

<RegistryLogsDialog bind:this={logDialog} />
<RegistryTagDeleteDialog bind:this={deleteTagDialog} on:delete={() => dispatch('refresh')} />
