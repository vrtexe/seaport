<script lang="ts" context="module">
  import { Color, Style, type AnsiItem } from '$lib/components/pages/dashboard/applications/AnsiText.svelte';

  const regex = /\u001B\[(?:(?<style>\w+);)?(?<color>\w+)m/;
  const regexSplitter = /(\u001B\[(?:\w+;)?(?:\w+)m)/;

  export function parseAnsi(text: string) {
    const collector: AnsiItem[] = [];
    let current: AnsiItem | undefined;

    for (const elem of text.split(regexSplitter)) {
      if (regex.test(elem)) {
        const regResult = regex.exec(elem);
        const styleGroup = regResult?.groups?.['style'];
        const colorGroup = regResult?.groups?.['color'];

        const style = styleGroup ? Style[styleGroup] : undefined;
        const color = Color[colorGroup! as keyof typeof Color];
        if (color === 'reset') {
          current = undefined;
        }

        if (!current) {
          current = {
            style,
            color,
            text: '',
            children: []
          };
          collector.push(current);
        } else {
          const next = {
            style,
            color,
            text: '',
            children: []
          };
          current.children.push(next);
          current = next;
        }
      } else {
        if (current) {
          current.text = elem;
        } else {
          collector.push({
            text: elem,
            children: []
          });
        }
      }
    }

    return collector;
  }
</script>

<script lang="ts">
  import AnsiText from '$lib/components/pages/dashboard/applications/AnsiText.svelte';

  export let values: AnsiItem[];
</script>

{#each values as value}
  <AnsiText {value} />
{/each}
