<script lang="ts">
  import Input from '$lib/components/Input.svelte';
  import type { Image } from '$lib/types/baseImageRequest';

  export let image: Image;
</script>

<div class="flex gap-4 py-4">
  <Input id="image-name" name="app-name" bind:value={image.data.name} placeholder="my-app">Application name</Input>
  <Input id="image-version" name="app-version" bind:value={image.data.version} placeholder="1.0.0">
    Application version
  </Input>
</div>
