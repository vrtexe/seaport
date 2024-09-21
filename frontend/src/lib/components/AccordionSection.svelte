<script lang="ts" context="module">
  export type Field<T> = {
    id: string;
    title: string;
    intoView?: boolean;
    data: T;
    deletable: boolean;
    subFields?: Field<T>[];
  };
</script>

<script lang="ts">
  import { intoView } from '$lib/actions';

  type T = $$Generic;

  export let field: Field<T>;
</script>

<div
  id={field.id}
  use:intoView
  class="form-section mb-4 mt-1 rounded-lg border px-5 py-5 pt-4 first:mt-6 last:mb-0"
  on:enterIntoView={() => (field.intoView = true)}
  on:exitOutOfView={() => (field.intoView = false)}>
  <span class="text-lg font-bold opacity-60">{field.title}</span>
  <div>
    <slot name="content" id={field.id} data={field.data} />
  </div>
  {#if field.subFields}
    {#each field.subFields as subField}
      <div
        id={subField.id}
        class="form-section mt-2 pl-0 pt-8"
        use:intoView
        on:enterIntoView={() => (subField.intoView = true)}
        on:exitOutOfView={() => (subField.intoView = false)}>
        <span class="text-sm font-bold opacity-60">{subField.title}</span>
        <div>
          <slot name="sub-content" id={field.id} subId={subField.id} data={field.data} />
        </div>
      </div>
    {/each}
  {/if}
</div>
