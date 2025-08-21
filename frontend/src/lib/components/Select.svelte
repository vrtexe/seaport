<script lang="ts" context="module">
  export type SelectOption<T> = { value: T; name: string; custom?: boolean };
</script>

<script lang="ts">
  import Select from 'svelte-select';
  import ChevronDown from 'svelte-material-icons/ChevronDown.svelte';
  import Close from 'svelte-material-icons/Close.svelte';

  type T = $$Generic;

  export let id: string;
  export let name: string;

  export let value: T | undefined;
  export let options: SelectOption<T>[];
  export let disabled = false;

  export let createValue: ((value: string) => T) | undefined = undefined;

  const handleChange = (e: CustomEvent<SelectOption<T>>) => {
    if (!e.detail.custom) {
      options = options.filter(v => !v.custom);
    }
    value = e.detail.value;
  };

  const handleClear = () => {
    value = undefined;
    options = options.filter(v => !v.custom);
  };

  let filterText = '';
  function handleFilter(e: CustomEvent<string>) {
    if (!createValue) return;

    if (e.detail.length === 0 && filterText.length > 0) {
      const prev = options.filter(v => !v.custom);
      options = [...prev, { value: createValue(filterText), name: filterText, custom: true }];
    }
  }

  $: internalValue = options.find(d => d.value === value);
</script>

<div class="flex flex-col gap-1">
  <label for={id} class="w-full text-xs opacity-45">
    <slot />
  </label>
  <Select
    value={internalValue}
    items={options}
    {id}
    {name}
    bind:filterText
    on:change={handleChange}
    on:clear={handleClear}
    on:filter={handleFilter}
    --border-radius="0.25rem"
    --max-height="38px"
    --border="1px solid rgb(229 231 235 / var(--tw-border-opacity))"
    --border-hover=" rgb(209 213 219 / var(--tw-border-opacity)) "
    --border-focused="1px solid var(--primary-color)"
    --value-container-padding="0.5rem 0"
    --clear-select-width="2rem"
    --chevron-width="2rem"
    --clear-select-focus-outline="1px solid var(--primary-color)"
    {disabled}
    showChevron
    itemFilter={(l, i, o) => o.name.includes(i)}
    class="min-w-56 rounded border border-gray-200 bg-white px-4 py-2 shadow outline-primary marker:ml-4 marker:border-r-8"
    inputAttributes={{
      class: 'focus:outline outline-primary'
    }}>
    <svelte:fragment slot="item" let:item>
      {item.name ?? ''}
    </svelte:fragment>
    <svelte:fragment slot="selection" let:selection>
      {selection.name ?? ''}
    </svelte:fragment>
    <svelte:fragment slot="clear-icon">
      <span class="flex h-full w-full items-center justify-center opacity-65 hover:cursor-pointer hover:opacity-100">
        <Close class="font-normal" size="1.25rem" />
      </span>
    </svelte:fragment>
    <svelte:fragment slot="chevron-icon">
      <span class="flex h-full w-full items-center justify-center hover:cursor-pointer">
        <ChevronDown class="font-normal" size="1.5rem" />
      </span>
    </svelte:fragment>
  </Select>
</div>
