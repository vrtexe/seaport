<script lang="ts">
  import { page } from '$app/stores';
  import { onMount } from 'svelte';
  import type { ImageDetails } from '$lib/generated';
  import { getRegistryImage } from '$lib/service/registryService';
  import RegistryDetails from '$lib/components/pages/admin/registry/RegistryDetails.svelte';

  let image: ImageDetails | undefined;

  onMount(() => {
    load();
  });

  function load() {
    const id = parseInt($page.params['id']);
    loadData(id);
  }

  async function loadData(id: number) {
    image = await getRegistryImage(id);
  }
</script>

{#if image}
  <RegistryDetails {image} on:refresh={() => load()} />
{/if}
