<script>
  import Package from 'svelte-material-icons/Package.svelte';
</script>

<main class="flex w-full justify-center">
  <div class="flex h-fit flex-col gap-6 py-32">
    <div class="flex justify-center"><Package class="text-[5em]" /></div>
    <h2 class="text-center text-2xl">You have not yet hosted any applications</h2>
    <div class="flex justify-center gap-8">
      <a href="/application/create" class="primary-button text-xl">Host your first application</a>
      <a href="/docs" class="primary-button text-xl">Read documentation</a>
    </div>
  </div>
</main>
