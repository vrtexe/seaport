<script lang="ts">
  import Button from '$lib/components/buttons/Button.svelte';
  import type { Pagination } from '$lib/generated';
  import { createEventDispatcher } from 'svelte';
  import ChevronDoubleLeft from 'svelte-material-icons/ChevronDoubleLeft.svelte';
  import ChevronDoubleRight from 'svelte-material-icons/ChevronDoubleRight.svelte';
  import ChevronLeft from 'svelte-material-icons/ChevronLeft.svelte';
  import ChevronRight from 'svelte-material-icons/ChevronRight.svelte';

  const dispatch = createEventDispatcher<{
    page: number;
  }>();

  export let value: Pagination = {
    page: 1,
    size: 20,
    totalElements: 300,
    totalPages: 10
  };

  const goToPage = (page: number) => {
    dispatch('page', page);
  };
</script>

<div class="flex h-20 gap-2 py-1">
  <div class="h-full py-4">
    <Button
      class="flex h-full items-center justify-center !px-2 disabled:bg-white disabled:text-gray-400"
      disabled={value.page === 1}
      on:click={() => goToPage(1)}>
      <ChevronDoubleLeft class="inline-block" height="1.5em" width="1.5em" />
    </Button>
  </div>
  <div class="h-full py-4">
    <Button
      class="flex h-full items-center justify-center !px-2 disabled:bg-white disabled:text-gray-400"
      disabled={value.page === 1}
      on:click={() => goToPage(value.page - 1)}>
      <ChevronLeft class="inline-block" height="1.5em" width="1.5em" />
    </Button>
  </div>

  <div class="flex h-full w-full gap-2 overflow-auto py-4">
    {#each { length: value.totalPages } as _, page}
      <Button
        class="h-full bg-white hover:text-primary active:bg-white active:brightness-90 disabled:bg-primary disabled:text-white"
        disabled={value.page === page + 1}
        on:click={() => goToPage(page + 1)}>
        {page + 1}
      </Button>
    {/each}
  </div>
  <!-- 
  {#each { length: Math.min(value.page - 1, Math.min(value.page - 1, count) + Math.max(count - (value.totalPages - value.page), 0)) } as __, page}
  {/each} -->

  <!-- <Button
    class="bg-white hover:text-primary active:bg-white active:brightness-90 disabled:bg-primary disabled:text-white"
    disabled={true}>{value.page}</Button>

  {#each { length: Math.min(value.totalPages - value.page, 5) + Math.max(count - value.page, 0) } as __, page}
    <Button
      class="bg-white hover:text-primary active:bg-white active:brightness-90 disabled:bg-primary disabled:text-white"
      >{page + 1 + value.page}</Button>
  {/each}

  {#if value.page + count < value.totalPages}
    ...
    <Button class="disabled:bg-white disabled:text-gray-400">{value.totalPages}</Button>
  {/if} -->
  <div class="h-full py-4">
    <Button
      class="flex h-full items-center justify-center !px-2 hover:text-primary active:bg-white active:brightness-90 disabled:bg-white disabled:text-gray-400"
      disabled={value.page === value.totalPages}
      on:click={() => goToPage(value.page + 1)}>
      <ChevronRight class="inline-block" height="1.5em" width="1.5em" />
    </Button>
  </div>
  <div class="h-full py-4">
    <Button
      class="flex h-full items-center justify-center !px-2 hover:text-primary active:bg-white active:brightness-90 disabled:bg-white disabled:text-gray-400"
      disabled={value.page === value.totalPages}
      on:click={() => goToPage(value.totalPages)}>
      <ChevronDoubleRight class="inline-block" height="1.5em" width="1.5em" />
    </Button>
  </div>
</div>
