<script lang="ts">
  import { page } from '$app/stores';
  import '$lib/styles/app.css';

  let header: HTMLElement | undefined;
  let headerHeight: number | undefined;

  $: headerStyle = header && getComputedStyle(header);
  $: borderBottomWidth = headerStyle?.borderBottomWidth;
</script>

<header
  bind:this={header}
  bind:clientHeight={headerHeight}
  class="sticky top-0 z-50 flex items-center gap-16 border-b-2 border-gray-300 bg-white shadow-md">
  <a href="/" class="flex gap-2 px-2 py-4">
    <!-- <img alt="l" /> -->
    <h1>Hosting</h1>
  </a>

  <nav class="flex items-center">
    <ul class="flex gap-2">
      <li>
        <a class="inline-block px-2 py-4" href="/" class:active={$page.route?.id === '/'}>
          <span>Dashboard</span>
        </a>
      </li>
      <!-- <li>
        <a class="inline-block px-2 py-4" href="/explore" class:active={$page.route?.id === '/explore'}>
          <span>Explore</span>
        </a>
      </li> -->
      <!-- <li>
        <a class="inline-block px-2 py-4" href="/docs" class:active={$page.route?.id === '/docs'}>
          <span>Documentation</span>
        </a>
      </li> -->
    </ul>
  </nav>

  <div class="flex h-full flex-1 justify-end gap-4 px-8">
    <!-- <button class="primary" type="button">Login</button> -->
    <!-- <button class="primary" type="button">Register</button> -->
  </div>
</header>

<div style:height="calc(100% - {headerHeight ?? 0}px - {borderBottomWidth ?? '0px'})">
  <slot />
</div>

<!-- </div> -->

<style lang="postcss">
  a.active span {
    @apply border-b border-blue-500 pb-2 text-blue-500;
  }
</style>
