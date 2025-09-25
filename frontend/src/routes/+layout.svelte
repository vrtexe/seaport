<script lang="ts" context="module">
  export type Page = (typeof Page)[keyof typeof Page];
  export const Page = Object.freeze({
    Documentation: '/(dashboard)/documentation',
    Explore: '/(dashboard)/explore',
    Dashboard: '/(dashboard)',
    Admin: '/admin'
  } as const);

  const Route: Record<Page, string> = {
    [Page.Dashboard]: '/',
    [Page.Explore]: '/explore',
    [Page.Documentation]: '/documentation',
    [Page.Admin]: '/admin'
  };

  export const PageValues: Page[] = Object.values(Page);
</script>

<script lang="ts">
  import { page } from '$app/stores';
  import '$lib/styles/app.css';
  import SeaPort from '$lib/assets/SeaPort.svelte';
  import SpLine from '$lib/assets/SPLine.svelte';
  import { onMount } from 'svelte';
  import { initKeycloak, login, logout, register, user } from '$lib/service/keycloakService';
  import { isAdmin } from '$lib/model/user';
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';

  let header: HTMLElement | undefined;
  let headerHeight: number | undefined;

  onMount(() => {
    initKeycloak();
  });

  $: headerStyle = header && getComputedStyle(header);
  $: borderBottomWidth = headerStyle?.borderBottomWidth;

  $: activePage = PageValues.find(v => $page.route.id?.startsWith(v));
</script>

<header
  bind:this={header}
  bind:clientHeight={headerHeight}
  class="sticky top-0 z-50 flex items-center gap-8 border-b-2 border-gray-300 bg-white shadow-md">
  <a href="/" class="flex gap-4 px-4">
    <div class="text-4xl">
      <SpLine />
    </div>
    <h1 class="flex items-center justify-center text-xl">
      <SeaPort />
    </h1>
  </a>

  <nav class="flex items-center">
    <ul class="flex gap-2">
      <li>
        <a class="inline-block px-2 py-4" href={Route[Page.Dashboard]} class:active={activePage === Page.Dashboard}>
          <span>Dashboard</span>
        </a>
      </li>
      {#if $user && isAdmin($user)}
        <li>
          <a class="inline-block px-2 py-4" href={Route[Page.Admin]} class:active={activePage === Page.Admin}>
            <span>Admin</span>
          </a>
        </li>
      {/if}
      <!-- <li>
        <a class="inline-block px-2 py-4" href="/explore" class:active={$page.route?.id === '/explore'}>
          <span>Explore</span>
        </a>
      </li> -->
      <li>
        <a class="inline-block px-2 py-4" href="/docs" class:active={$page.route?.id === '/docs'}>
          <span>Documentation</span>
        </a>
      </li>
    </ul>
  </nav>

  <div class="flex h-full flex-1 justify-end gap-4 px-8">
    {#if !$user}
      <PrimaryButton on:click={login}>Login</PrimaryButton>
      <PrimaryButton on:click={register}>Register</PrimaryButton>
    {:else}
      <PrimaryButton on:click={logout}>Logout</PrimaryButton>
    {/if}
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
