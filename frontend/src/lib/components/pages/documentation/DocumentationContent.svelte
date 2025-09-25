<script lang="ts">
  import SideBar from '$lib/components/SideBar.svelte';
  import LinkSideBar from '$lib/components/sidebar/LinkSideBar.svelte';
  import { page } from '$app/stores';
</script>

<main class="flex h-full w-full">
  <div class="h-full">
    <SideBar class="fixed py-2">
      <LinkSideBar page={$page.route.id} />
    </SideBar>
  </div>
  <div class="h-full w-full overflow-auto">
    <div class="px-4 py-4">
      <h2 class="text-2xl font-semibold">Overview</h2>
    </div>

    <slot />
  </div>
</main>
