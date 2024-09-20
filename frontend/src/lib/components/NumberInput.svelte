<script lang="ts">
  type T = $$Generic;

  export let id: string;
  export let name: string;
  export let value: number;

  export let max: number | undefined = undefined;
  export let min: number | undefined = undefined;

  export let placeholder: string = '';
  export let inputClass: string = '';

  $: max != undefined && value > max && (value = max);
  $: min != undefined && value < min && (value = min);
</script>

<div class="flex min-w-56 flex-col gap-1">
  <label for={id} class="text-xs opacity-45">
    <slot />
  </label>

  <input
    bind:value
    {id}
    {name}
    {min}
    {max}
    maxlength={max?.toString().length}
    class="rounded border border-gray-200 px-4 py-1.5 shadow {inputClass}"
    type="number"
    {placeholder} />
</div>

<style>
  input[type='number']::-webkit-outer-spin-button,
  input[type='number']::-webkit-inner-spin-button,
  input[type='number'] {
    -webkit-appearance: none;
    /* margin: 0; */
    /* -moz-appearance: textfield !important; */
    /* appearance: textfield; */
  }
</style>
