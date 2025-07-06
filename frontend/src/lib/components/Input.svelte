<script lang="ts">
  type T = $$Generic;

  export let id: string;
  export let name: string;
  export let value: string;
  export let required: boolean = false;
  export let readonly: boolean = false;
  export let disabled: boolean = false;
  export let placeholder: string = '';
  export let inputClass: string = '';
  export let labelClass: string = '';
  export let inputStyle: string = '';

  let interacted = false;

  export const interact = () => {
    interacted = true;
  };
</script>

<div class="input-container relative flex min-w-56 flex-col gap-1">
  <label for={id} class="text-xs opacity-45 {labelClass}">
    <slot />
    {required ? '*' : ''}
  </label>

  <input
    bind:value
    {id}
    {name}
    {readonly}
    {required}
    {disabled}
    class="rounded border border-gray-200 px-4 py-2 shadow outline-primary read-only:opacity-65 read-only:outline-none hover:border-gray-300 focus:outline {inputClass}"
    class:interacted
    style={inputStyle}
    type="text"
    on:blur={() => (interacted = true)}
    on:input
    {placeholder} />
  <slot name="other" />
</div>

<style lang="postcss">
  .input-container:has(> .interacted:invalid) label,
  .input-container:has(> input:user-invalid) label {
    color: theme(colors.red.500);
    opacity: 100;
  }

  .interacted:invalid,
  input:user-invalid {
    outline: 1px solid theme(colors.red.500);
  }
</style>
