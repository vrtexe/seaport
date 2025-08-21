<script lang="ts">
  import loader from '@monaco-editor/loader';
  import type * as Monaco from 'monaco-editor/esm/vs/editor/editor.api';
  import { onDestroy, onMount } from 'svelte';

  let editor: Monaco.editor.IStandaloneCodeEditor;
  let monaco: typeof Monaco;
  let editorContainer: HTMLElement;

  // Define props with Svelte 5 syntax
  // interface Props {
  export let value: string;
  export let language: string | undefined = undefined;
  export let theme: string = 'vs-dark';
  let className: string = '';

  export { className as class };
  // }

  // let { value = $bindable(), language = 'html', theme = 'vs-dark' }: Props = $props();

  onMount(() => {
    (async () => {
      // Remove the next two lines to load the monaco editor from a CDN
      // see https://www.npmjs.com/package/@monaco-editor/loader#config
      const monacoEditor = await import('monaco-editor');
      loader.config({ monaco: monacoEditor.default });

      monaco = await loader.init();

      // Your monaco instance is ready, let's display some code!
      editor = monaco.editor.create(editorContainer, {
        value,
        language,
        theme,
        automaticLayout: true,
        overviewRulerLanes: 0,
        overviewRulerBorder: false,
        wordWrap: 'on'
      });

      editor.onDidChangeModelContent(e => {
        if (e.isFlush) {
          // true if setValue call
          //console.log('setValue call');
          /* editor.setValue(value); */
        } else {
          // console.log('user input');
          const updatedValue = editor?.getValue() ?? ' ';
          value = updatedValue;
        }
      });
    })();
  });

  $: if (value) {
    if (editor) {
      // check if the editor is focused
      if (editor.hasWidgetFocus()) {
        // let the user edit with no interference
      } else {
        if (editor?.getValue() ?? ' ' !== value) {
          editor?.setValue(value);
        }
      }
    }
  }

  $: if (value === '') {
    editor?.setValue(' ');
  }

  onDestroy(() => {
    monaco?.editor.getModels().forEach(model => model.dispose());
    editor?.dispose();
  });
</script>

<div class="container w-full overflow-hidden rounded-lg border p-0 {className}" bind:this={editorContainer}></div>
