<script lang="ts">
  import { page } from '$app/stores';
  import PrimaryLinkButton from '$lib/components/buttons/PrimaryLinkButton.svelte';
  import { getImage } from '$lib/service/imageService';
  import { onMount } from 'svelte';
  import Plus from 'svelte-material-icons/Plus.svelte';
  import ArrowLeft from 'svelte-material-icons/ArrowLeft.svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import type { ImageDetails } from '$lib/generated';
  import ApplicationDetails from '$lib/components/pages/dashboard/applications/ApplicationDetails.svelte';

  let image: ImageDetails | undefined;

  onMount(() => {
    load();
  });

  function load() {
    const id = parseInt($page.params['id']);
    loadData(id);
  }

  async function loadData(id: number) {
    image = await getImage(id);
  }
</script>

{#if image}
  <ApplicationDetails {image} on:release={() => load()} />
{/if}
