<script lang="ts" context="module">
  export type Page = (typeof Page)[keyof typeof Page];
  export const Page = Object.freeze({
    Storage: '/admin/storage',
    Registry: '/admin/registry',
    Image: '/admin/image'
  } as const);

  const Route: Record<Page, string> = {
    [Page.Image]: '/admin/image',
    [Page.Storage]: '/admin/storage',
    [Page.Registry]: '/admin/registry'
  };

  export const PageValues: Page[] = Object.values(Page);
</script>

<script lang="ts">
  import LinkButton from '$lib/components/sidebar/LinkButton.svelte';

  export let page: string | null;

  $: activePage = PageValues.find(v => page?.startsWith(v));
</script>

<div class="grid gap-2 px-4 py-6">
  <LinkButton href={Route[Page.Image]} active={activePage === Page.Image}>Image</LinkButton>
  <LinkButton href={Route[Page.Storage]} active={activePage === Page.Storage}>Storage</LinkButton>
  <LinkButton href={Route[Page.Registry]} active={activePage === Page.Registry}>Registry</LinkButton>
</div>
