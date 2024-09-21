<script lang="ts" context="module">
  export type DeleteEvent = { id: string };
</script>

<script lang="ts">
  import { createEventDispatcher } from 'svelte';
  import AccordionSection, { type Field } from '$lib/components/AccordionSection.svelte';
  import SideBar from '$lib/components/SideBar.svelte';
  import ArrowUpBoldOutline from 'svelte-material-icons/ArrowUpBoldOutline.svelte';
  import TrashCanOutline from 'svelte-material-icons/TrashCanOutline.svelte';

  const dispatch = createEventDispatcher<{
    delete: DeleteEvent;
  }>();

  type T = $$Generic;

  export let fields: Field<T>[];

  let sideBarWidth: number | undefined;
  let pageContent: HTMLDivElement | undefined;

  function scrollToTop() {
    pageContent?.scrollTo(0, 0);
  }
</script>

<div class="flex h-full">
  <slot name="additional-sidebar-content" />
  <div class="h-full">
    <SideBar bind:width={sideBarWidth} class="fixed  py-2">
      <div class="grid h-full grid-rows-[auto_1fr_auto]">
        <div class="px-4">
          <slot name="sidebar-actions" />
        </div>
        <ul class="my-4 overflow-auto px-4">
          {#each fields as field}
            <li class:into-view={field.intoView}>
              <details open class="flex select-none flex-col gap-3">
                <summary class="cursor-pointer marker:text-xl" class:hide-marker={!field.subFields?.length}>
                  <a href="#{field.id}" class=" mr-auto inline-block px-2 py-1">{field.title}</a>
                  {#if field.deletable}
                    <button
                      class="inline-block px-4 py-1"
                      type="button"
                      on:click={() => dispatch('delete', { id: field.id })}>
                      <TrashCanOutline class="inline-block" height="1.4em" width="1.4em" />
                    </button>
                  {/if}
                </summary>
                {#if field.subFields}
                  <ul>
                    {#each field.subFields as subField}
                      <li class="flex items-center py-1 pl-10" class:into-view-sub={subField.intoView}>
                        <a href="#{subField.id}">{subField.title}</a>
                      </li>
                    {/each}
                  </ul>
                {/if}
              </details>
            </li>
          {/each}
        </ul>
        <div class="px-4">
          <slot name="sidebar-end" />
        </div>
      </div>
    </SideBar>
  </div>

  <div bind:this={pageContent} class="h-full flex-1 overflow-scroll px-10">
    <slot name="complete-content" {fields}>
      {#each fields as field}
        <AccordionSection bind:field>
          <svelte:fragment slot="content" let:id let:data>
            <slot name="content" {id} {data} />
          </svelte:fragment>

          <svelte:fragment slot="sub-content" let:id let:subId let:data>
            <slot name="sub-content" {id} {subId} {data} />
          </svelte:fragment>
        </AccordionSection>
      {/each}
    </slot>

    <div style:height="100%" />

    <slot name="additional-content" />

    <div class="fixed bottom-4 right-4">
      <button class="primary-circle" type="button" on:click={scrollToTop}>
        <ArrowUpBoldOutline height="1.25em" width="1.25em" />
      </button>
    </div>
  </div>
</div>

<style lang="postcss">
  li:not(.into-view) + li.into-view summary,
  li.into-view:first-child summary,
  li:not(.into-view-sub) + li.into-view-sub,
  li:not(.into-view) + li.into-view ul li.into-view-sub:first-child,
  li.into-view:first-child ul li.into-view-sub:first-child {
    @apply text-blue-400;
  }

  summary.hide-marker::marker {
    content: '\00a0\00a0';
    letter-spacing: 0.18em;
  }
</style>
