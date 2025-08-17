<script lang="ts">
  export let active = false;
  export let href: string;
</script>

<a
  {href}
  class="w-full select-none rounded-md border border-gray-300 py-2 text-center font-bold text-black active:opacity-100 active:brightness-75 disabled:brightness-100
    {active
    ? 'pointer-events-none cursor-default select-none bg-primary text-white'
    : 'cursor-pointer bg-white hover:text-secondary'}">
  <slot />
</a>
