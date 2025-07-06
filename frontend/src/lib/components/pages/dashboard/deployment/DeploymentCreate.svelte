<script lang="ts" context="module">
  type DeploymentData = {
    group?: Group;
    // image?: Image | undefined;
    imageTag?: ImageTag | undefined;
    deployment: {
      name: string;
      port: number;
    };
    service: {
      name: string;
    };
    ingress: {
      enabled: boolean;
      name: string;
      path: string;
    };
    environment: EnvironmentData;
  };

  const mapRequest = (data: DeploymentData): DeploymentCreateRequest => {
    if (data.group?.id == undefined) {
      throw new Error('Invalid group');
    }

    if (data.imageTag?.id == undefined) {
      throw new Error('Invalid application provided');
    }

    if (!data.deployment.name) {
      throw new Error('Invalid deployment name');
    }

    return {
      groupId: data.group.id,
      deployment: {
        name: data.deployment.name,
        state: DeploymentState.Initial,
        imageTagId: data.imageTag.id,
        exposedPort: data.deployment.port,
        environment: Object.fromEntries(data.environment.values.map(p => [p.name, p.value]))
      },
      service: {
        name: data.service.name || `${data.deployment.name}-service`
      },
      ...(data.ingress.enabled
        ? {
            ingress: {
              name: data.ingress.name || `${data.deployment.name}-external`,
              path: data.ingress.path || `${data.deployment.name}`
            }
          }
        : {})
    };
  };
</script>

<script lang="ts">
  import PrimaryButton from '$lib/components/buttons/PrimaryButton.svelte';
  import SecondaryButton from '$lib/components/buttons/SecondaryButton.svelte';
  import Input from '$lib/components/Input.svelte';
  import NumberInput from '$lib/components/NumberInput.svelte';
  import Select from '$lib/components/Select.svelte';
  import {
    DeploymentState,
    type DeploymentCreateRequest,
    type Group,
    type Image,
    type ImageDetails,
    type ImageTag,
    type Namespace
  } from '$lib/generated';
  import { getAllGroups } from '$lib/service/groupService';
  import { getImage, getImages } from '$lib/service/imageService';
  import { onMount } from 'svelte';
  import OpenInNew from 'svelte-material-icons/OpenInNew.svelte';
  import type { Environment as EnvironmentData } from '$lib/components/pages/dashboard/deployment/types';
  import EnvironmentDialog from '$lib/components/pages/dashboard/deployment/EnvironmentDialog.svelte';
  import Environment from '$lib/components/pages/dashboard/deployment/Environment.svelte';
  import type { NativeEvent } from '$lib/types/svelte';
  import { createDeployment } from '$lib/service/deploymentService';
  import Notification, { NotificationType } from '$lib/components/pages/dashboard/deployment/Notification.svelte';
  import { ClientError } from '$lib/errors/ClientError';
  import { getUserNamespace } from '$lib/service/namespaceService';

  let data: DeploymentData = {
    deployment: {
      name: '',
      port: 8080
    },
    service: {
      name: ''
    },
    ingress: {
      enabled: false,
      name: '',
      path: ''
    },
    environment: {
      values: []
    }
  };

  let image: Image | undefined;

  let groups: Group[] = [];
  let images: Image[] = [];
  let imageDetails: ImageDetails | undefined;

  let notification: Notification | undefined;

  let environmentDialog: EnvironmentDialog | undefined;

  let namespace: Namespace | undefined;

  onMount(() => {
    loadGroups();
    loadImages();
    loadNamespace();
  });

  const loadNamespace = async () => {
    namespace = await getUserNamespace();
  };

  const loadGroups = async () => {
    groups = await getAllGroups().then(s => s.data);
    data.group = groups[0];
  };

  const loadImages = async () => {
    images = await getImages().then(s => s.data);
    image = images[0];
  };

  const loadImageTags = async (image: Image | undefined) => {
    if (!image?.id) {
      data.imageTag = undefined;
      return;
    }
    imageDetails = await getImage(image.id);
    data.imageTag = imageDetails.tags[0];
  };

  let rect: DOMRectReadOnly | undefined;

  const handlePathInput = (e: NativeEvent<Event, HTMLInputElement>) => {
    e.currentTarget.value = e.currentTarget.value.replace(' ', '');
  };

  const handleSave = async () => {
    try {
      const request = mapRequest(data);
      await createDeployment(request);
      history.back();
    } catch (e) {
      console.error(e);

      if (e instanceof Error) {
        notification?.open({
          type: NotificationType.Error,
          text: e.message
        });
      } else if (e instanceof ClientError) {
        notification?.open({
          type: NotificationType.Error,
          text: e.message
        });
      }
    }
  };

  const isInvalid = (data: DeploymentData) => {
    return !data.deployment.name || !data.imageTag;
  };

  $: loadImageTags(image);
</script>

<div class="flex h-full flex-col gap-8 px-6 py-4">
  <div class="flex justify-between">
    <h2 class="text-2xl font-bold">Create Deployment</h2>
    <div class="flex gap-2">
      <SecondaryButton href="/deployment">Cancel</SecondaryButton>
      <PrimaryButton disabled={isInvalid(data)} on:click={handleSave}>Save</PrimaryButton>
    </div>
  </div>
  <div class="flex flex-1 flex-col justify-between pb-4">
    <form class="grid gap-8">
      <fieldset class="grid grid-cols-2 gap-4">
        <legend class="pb-6 text-xs font-bold text-gray-400">General</legend>
        <Select id="group" name="group" options={groups.map(s => ({ value: s, name: s.name }))} bind:value={data.group}>
          Group
        </Select>
      </fieldset>

      <fieldset class="grid gap-4">
        <legend class="pb-6 text-xs font-bold text-gray-400">Deployment</legend>
        <div class="grid grid-cols-2 gap-4">
          <Select
            id="application-name"
            name="application-name"
            options={images.map(s => ({ value: s, name: s.name }))}
            bind:value={image}>Application name</Select>
          <Select
            id="application-version"
            name="application-version"
            options={imageDetails?.tags.map(s => ({ value: s, name: s.version })) ?? []}
            bind:value={data.imageTag}>Application version</Select>
        </div>
        <div class="grid grid-cols-[2fr_1fr] gap-4">
          <Input
            id="deployment-name"
            name="deployment-name"
            required
            bind:value={data.deployment.name}
            on:input={handlePathInput}>Name</Input>
          <NumberInput
            id="application-port"
            name="application-port"
            bind:value={data.deployment.port}
            max={65535}
            required>
            Port
          </NumberInput>
        </div>
      </fieldset>

      <fieldset class="grid gap-4">
        <legend class="pb-6 text-xs font-bold text-gray-400">Service</legend>
        <Input
          id="service-name"
          name="service-name"
          placeholder="{data.deployment.name || 'deployment'}-service"
          on:input={handlePathInput}
          bind:value={data.service.name}>
          Name
        </Input>
      </fieldset>

      <fieldset class="grid gap-4">
        <legend class="flex gap-2 pb-6 text-xs font-bold text-gray-400">
          <input
            type="checkbox"
            name="external-enable"
            id="external-enable"
            class="cursor-pointer accent-primary"
            bind:checked={data.ingress.enabled}
            required />
          <label for="external-enable" class="cursor-pointer select-none">External</label>
        </legend>

        <Input
          id="external-name"
          name="external-name"
          disabled={!data.ingress.enabled}
          placeholder="{data.deployment.name || 'deployment'}-external"
          bind:value={data.ingress.name}>
          Name
        </Input>
        <div class="relative">
          <Input
            id="external-path"
            name="external-path"
            placeholder={data.deployment.name || 'deployment'}
            disabled={!data.ingress.enabled}
            inputStyle="padding-left: {(rect?.right ?? 0) + 2}px;"
            on:input={handlePathInput}
            bind:value={data.ingress.path}>Path</Input>
          <span
            bind:contentRect={rect}
            class="absolute bottom-0 left-0 border border-transparent py-2 pl-4 text-gray-300">
            /{namespace?.name ?? ''}/
          </span>
        </div>
      </fieldset>

      <fieldset>
        <legend class="flex w-full justify-between pb-6 text-xs font-bold text-gray-400">
          <span class="flex items-center">Environment</span>
          <div class="flex gap-2">
            <button
              class="rounded-lg border-2 border-primary bg-white p-1 text-primary opacity-65 brightness-100 hover:opacity-100 active:brightness-90"
              type="button"
              on:click={() => environmentDialog?.open(data.environment)}>
              <OpenInNew size="1.5em" />
            </button>
          </div>
        </legend>

        <Environment bind:data={data.environment} />
      </fieldset>
    </form>
  </div>
</div>

<EnvironmentDialog bind:this={environmentDialog} on:save={e => (data.environment = e.detail)} />
<Notification bind:this={notification} />
