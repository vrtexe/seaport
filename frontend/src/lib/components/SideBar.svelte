<script lang="ts">
  import type { NativeEvent } from '$lib/types/svelte';

  const MIN_WIDTH = 4;
  const MAX_WIDTH = 40;

  export let width = 20;
  export { className as class };

  let className = '';
  let resizing = false;

  function handleMouseDown(e: NativeEvent<MouseEvent, unknown>) {
    if (e.button === 0) {
      resizing = true;
      document.body.classList.add('cursor-ew-resize');
    }
  }

  function handleResize(e: NativeEvent<MouseEvent, Window>) {
    const newWidth = e.x / 16;
    if (newWidth >= MIN_WIDTH && newWidth <= MAX_WIDTH) {
      width = newWidth;
    }
  }

  function handleMouseUp() {
    resizing = false;
    document.body.classList.remove('cursor-ew-resize');
  }
</script>

<aside style:width="{width}rem" class="relative h-full w-80 overflow-auto bg-gray-100 {className}">
  <slot />

  <button
    role="spinbutton"
    tabindex="-1"
    type="button"
    class:cursor-ew-resize={resizing}
    class:hover:cursor-col-resize={!resizing}
    class="absolute right-0 top-0 flex h-full items-center bg-transparent pl-2"
    on:mousedown|preventDefault={handleMouseDown}>
    {#if resizing}
      <span class="absolute right-0 pr-2">&laquo;</span>
    {/if}
    <div class:resizing class=" h-full w-px bg-slate-400"></div>
    {#if resizing}
      <span class="absolute pl-2">&raquo;</span>
    {/if}
  </button>
</aside>

<svelte:window on:mousemove={resizing ? handleResize : undefined} on:mouseup={resizing ? handleMouseUp : undefined} />

<style lang="postcss">
  .resizing {
    @apply w-0.5 bg-slate-500;
  }
</style>
