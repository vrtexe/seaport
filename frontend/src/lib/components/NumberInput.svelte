<script lang="ts">
  type T = $$Generic;

  export let id: string;
  export let name: string;
  export let value: number;

  export let required: boolean = false;
  export let max: number | undefined = undefined;
  export let min: number | undefined = undefined;

  export let placeholder: string = '';
  export let inputClass: string = '';

  $: max != undefined && value > max && (value = max);
  $: min != undefined && value < min && (value = min);

  let interacted = false;

  export const interact = () => {
    interacted = true;
  };
</script>

<div class="input-container flex min-w-56 flex-col gap-1">
  <label for={id} class="text-xs opacity-45">
    <slot />
  </label>

  <input
    bind:value
    {id}
    {name}
    {min}
    {max}
    {required}
    maxlength={max?.toString().length}
    class="rounded border border-gray-200 px-4 py-2 shadow outline-primary read-only:opacity-65 read-only:outline-none hover:border-gray-300 focus:outline {inputClass}"
    class:interacted
    type="number"
    on:blur={() => (interacted = true)}
    {placeholder} />
</div>

<style lang="postcss">
  input[type='number']::-webkit-outer-spin-button,
  input[type='number']::-webkit-inner-spin-button,
  input[type='number'] {
    -webkit-appearance: none;
  }

  .interacted:invalid,
  input:user-invalid {
    outline: 1px solid theme(colors.red.500);
  }

  .input-container:has(> .interacted:invalid) label,
  .input-container:has(> input:user-invalid) label {
    color: theme(colors.red.500);
    opacity: 100;
  }
</style>
