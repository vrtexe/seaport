<script lang="ts">
  import Input from '$lib/components/Input.svelte';
  import NumberInput from '$lib/components/NumberInput.svelte';
  import Select, { type SelectOption } from '$lib/components/Select.svelte';
  import type { DeploymentDto, Image } from '$lib/types/baseImageRequest';

  export let id: string;
  export let deployment: DeploymentDto;
  export let images: Record<string, Image>;

  function resolveImageName(image: Image) {
    if (!image.data.name || !image.data.version) {
      return image.name;
    }

    return `${image.data.name}:${image.data.version}`;
  }

  $: imageOptions = Object.values(images).map(
    value =>
      ({
        name: resolveImageName(value),
        value: value.uid
      }) as SelectOption<string>
  );
</script>

<div class="flex flex-col gap-6 py-4">
  <div class="grid grid-cols-1 gap-4 xl:grid-cols-[2.5fr_0.25fr_1.25fr]">
    <Input id="{id}-language" name="language" bind:value={deployment.name}>Name</Input>
    <NumberInput id="{id}-port" name="port" max={65535} min={1} bind:value={deployment.port}>Port</NumberInput>
    <Select id="{id}-deploy" name="{id}-deploy" bind:value={deployment.image} options={imageOptions}>Deploy</Select>
  </div>
  <div class="flex gap-4">
    <div class="w-[50%]">
      <Input
        id="{id}-service-name"
        name="{id}-service-name"
        bind:value={deployment.service.name}
        inputClass="placeholder:text-stone-400 px-[6ch] w-full"
        placeholder={deployment.name && `${deployment.name}-service`}>
        Service name*
        <svelte:fragment slot="other">
          <div class="absolute bottom-0 border border-transparent py-1.5 pl-4 opacity-65">http://</div>
        </svelte:fragment>
      </Input>
    </div>
    <div class="w-[50%]">
      <Input id="{id}-path" name="path" bind:value={deployment.ingress.path} inputClass="pl-[10.85ch]">
        Path
        <svelte:fragment slot="other">
          <div class="absolute bottom-0 border border-transparent py-1.5 pl-4 opacity-65">/namespace/</div>
        </svelte:fragment>
      </Input>
    </div>
  </div>
</div>
