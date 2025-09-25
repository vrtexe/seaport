<script lang="ts" context="module">
  export type Page = (typeof Page)[keyof typeof Page];
  export const Page = Object.freeze({
    Deployments: '/deployment',
    Groups: '/group',
    Applications: '/(application)',
    Application: '/application'
  } as const);

  const Route: Record<Page, string> = {
    [Page.Applications]: '/',
    [Page.Groups]: Page.Groups,
    [Page.Deployments]: Page.Deployments,
    [Page.Application]: Page.Application
  };

  export const PageValues: Page[] = Object.values(Page);

  const applicationPages = <(string | undefined)[]>[Page.Applications, Page.Application];
</script>

<script lang="ts">
  import LinkButton from '$lib/components/sidebar/LinkButton.svelte';

  export let page: string | null;

  $: activePage = PageValues.find(v => page?.startsWith(`/(dashboard)${v}`));
</script>

<div class="grid gap-2 px-4 py-6">
  <LinkButton href={Route[Page.Applications]} active={applicationPages.includes(activePage)}>Applications</LinkButton>
  <LinkButton href={Route[Page.Groups]} active={activePage === Page.Groups}>Groups</LinkButton>
  <LinkButton href={Route[Page.Deployments]} active={activePage === Page.Deployments}>Deployment</LinkButton>
</div>
