<script lang="ts" context="module">
  type ColorEnum = (typeof ColorEnum)[keyof typeof ColorEnum];
  export const ColorEnum = {
    Reset: 'reset',
    Black: 'black',
    Red: 'red',
    Green: 'green',
    Yellow: 'yellow',
    Blue: 'blue',
    Purple: 'purple',
    Cyan: 'cyan',
    White: 'white'
  };

  export const Color: Record<string, ColorEnum> = {
    '0': ColorEnum.Reset,
    '30': ColorEnum.Black,
    '31': ColorEnum.Red,
    '32': ColorEnum.Green,
    '33': ColorEnum.Yellow,
    '34': ColorEnum.Blue,
    '35': ColorEnum.Purple,
    '36': ColorEnum.Cyan,
    '37': ColorEnum.White
  };

  type StyleEnum = (typeof StyleEnum)[keyof typeof StyleEnum];
  const StyleEnum = Object.freeze({
    Regular: 'regular',
    Bold: 'bold',
    Underline: 'underline'
  });
  export const Style: Record<string, StyleEnum> = {
    '0': StyleEnum.Regular,
    '1': StyleEnum.Bold,
    '4': StyleEnum.Underline
  };

  export type AnsiItem = {
    color?: string;
    style?: string;
    text: string;
    children: AnsiItem[];
  };
</script>

<script lang="ts">
  export let value: AnsiItem;
</script>

<span
  class:text-black={value.color === ColorEnum.Black}
  class:text-red-600={value.color === ColorEnum.Red}
  class:text-green-600={value.color === ColorEnum.Green}
  class:text-yellow-600={value.color === ColorEnum.Yellow}
  class:text-blue-600={value.color === ColorEnum.Blue}
  class:text-purple-600={value.color === ColorEnum.Purple}
  class:text-cyan-600={value.color === ColorEnum.Cyan}
  class:text-white={value.color === ColorEnum.White}
  class:font-bold={value.style === StyleEnum.Bold}
  class:underline={value.style === StyleEnum.Underline}>
  {value.text}
  {#each value.children as next}
    <svelte:self value={next} />
  {/each}
</span>
