<script>
  import { page } from '$app/stores';
  import ImageEditCreate from '$lib/components/pages/admin/image/ImageEditCreate.svelte';

  $: id = parseInt($page.params['id']);
</script>

{#if id != undefined}
  <ImageEditCreate {id} />
{/if}
