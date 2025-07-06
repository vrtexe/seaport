<script lang="ts" context="module">
  export type NotificationType = (typeof NotificationType)[keyof typeof NotificationType];
  export const NotificationType = Object.freeze({
    Error: 'ERROR',
    Info: 'INFO',
    Success: 'SUCCESS'
  } as const);

  export type Notification = {
    type: NotificationType;
    text: string;
  };
</script>

<script lang="ts">
  import AlertCircleOutline from 'svelte-material-icons/AlertCircleOutline.svelte';
  import InformationOutline from 'svelte-material-icons/InformationOutline.svelte';
  import CheckCircleOutline from 'svelte-material-icons/CheckCircleOutline.svelte';
  import Close from 'svelte-material-icons/Close.svelte';

  const INTERVAL = 20;
  const TIMEOUT_VALUE = 5000;

  let show = false;
  let data: Notification | undefined;
  let progress = 0;
  let interval: ReturnType<typeof setInterval> | undefined;
  let timeout: ReturnType<typeof setTimeout> | undefined;

  export const open = (value: Notification) => {
    closeDialog();

    data = value;
    show = true;
    interval = setInterval(() => (progress += INTERVAL), INTERVAL);
    timeout = setTimeout(closeDialog, TIMEOUT_VALUE);
  };

  export const closeDialog = () => {
    data = undefined;
    show = false;
    interval && clearInterval(interval);
    timeout && clearInterval(timeout);
    progress = 0;
  };
</script>

{#if data && show}
  <div
    class="absolute bottom-0 right-0 m-8 box-border flex min-h-16 min-w-80 items-start justify-between gap-2 rounded-lg border bg-white">
    <div class="flex h-full min-h-16 items-center justify-center gap-2 py-2 pl-4">
      <span class="text-red-500">
        {#if data.type === NotificationType.Error}
          <AlertCircleOutline class="text-red-600" size="2rem" />
        {:else if data.type === NotificationType.Info}
          <InformationOutline class="text-orange-400" size="2rem" />
        {:else if data.type === NotificationType.Success}
          <CheckCircleOutline class="text-green-600" size="2rem" />
        {/if}
      </span>
      <p class="max-w-80">{data.text}</p>
    </div>
    <button class="px-2 py-1" on:click={closeDialog}>
      <Close size="1.25rem" />
    </button>
    <progress class="absolute bottom-0 h-1 w-full" value={progress} max={TIMEOUT_VALUE} />
  </div>
{/if}

<style lang="postcss">
  progress::-moz-progress-bar {
    background: theme(colors.primary);
  }

  progress::-webkit-progress-value {
    background: theme(colors.primary);
  }

  progress::-webkit-progress-bar {
    background: white;
  }

  progress {
    background: white;
    color: theme(colors.primary);
  }
</style>
