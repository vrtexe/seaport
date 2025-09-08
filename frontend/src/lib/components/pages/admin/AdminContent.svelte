<script lang="ts">
  import SideBar from '$lib/components/SideBar.svelte';
  import { page } from '$app/stores';
  import AdminLinkSideBar from '$lib/components/pages/admin/AdminLinkSideBar.svelte';
</script>

<main class="flex h-full w-full">
  <div class="h-full">
    <SideBar class="fixed py-2">
      <AdminLinkSideBar page={$page.route.id} />
    </SideBar>
  </div>
  <div class="h-full w-full overflow-auto">
    <slot />
  </div>
</main>
