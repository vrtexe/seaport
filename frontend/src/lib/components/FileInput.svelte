<script lang="ts">
  import TrayArrowUp from 'svelte-material-icons/TrayArrowUp.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';

  function createFileList(files: File[]): FileList {
    let dataTransfer = new DataTransfer();
    for (const file of files) {
      dataTransfer.items.add(file);
    }

    return dataTransfer.files;
  }

  type T = $$Generic;

  export let id: string;
  export let name: string;
  export let value: FileList | undefined = undefined;
  export let placeholder: string = '';

  let dragging = false;
  let input: HTMLInputElement | undefined;
</script>

<div class="flex flex-col gap-1 rounded border border-gray-200 bg-white px-2 py-4 shadow">
  <label for={id} class="text-xs opacity-45">
    <slot />
  </label>
  <div class="relative z-0 flex h-full w-full flex-col">
    {#if value?.length}
      <div class=" w-full">
        {#each value as file}
          <div class=" flex w-full justify-between rounded border px-4 py-2 shadow-sm">
            <span>{file.name}</span>
            <button
              type="button"
              class=" relative z-50 cursor-pointer"
              on:click|preventDefault|stopPropagation={() => {
                let temp = new Set(value);
                temp.delete(file);
                value = createFileList([...temp]);
                input && (input.files = value);
              }}>
              <TrashCanOutline width="1.5em" height="1.5em" />
            </button>
          </div>
        {/each}
      </div>
    {/if}
    <!-- bind:files -->
    <input
      bind:files={value}
      bind:this={input}
      {id}
      {name}
      class="h-full min-h-48 w-full min-w-0 flex-1 bg-transparent px-4 py-1.5 text-[0px] file:right-0 file:h-full file:w-full file:rounded file:border file:border-dashed file:bg-transparent file:px-4 file:py-1 file:text-[1rem] file:hover:border-blue-400 file:hover:opacity-75"
      class:dragging
      class:remove-border={value}
      class:show-border={dragging}
      type="file"
      {placeholder}
      on:dragover={e => {
        dragging = true;
      }}
      on:dragleave={e => {
        dragging = false;
      }}
      on:drop={e => {
        console.log([...(e.dataTransfer?.items ?? [])]);
        console.log([...(e.currentTarget.files ?? [])]);
        dragging = false;
      }}
      on:dragend={e => {
        dragging = false;
      }} />
    <div
      class="absolute top-0 -z-10 flex h-full w-full flex-col items-center justify-center"
      class:dragging-content={dragging}>
      {#if !value?.length || dragging}
        <TrayArrowUp height="2rem" width="2rem" />
        <span class="text-lg">Drag and drop or choose a file</span>
      {/if}
    </div>
  </div>
</div>

<style lang="postcss">
  .dragging {
    @apply p-1;
  }

  input[type='file']:hover + div {
    @apply text-blue-400;
  }

  .dragging::file-selector-button {
    @apply z-40 border-2 border-blue-400;
  }

  .dragging-content {
    @apply opacity-40;
  }

  input[type='file']::file-selector-button {
    background-color: transparent;
    font-size: 0px;
  }

  .remove-border:not(.show-border)::file-selector-button {
    border: none;
  }
</style>
