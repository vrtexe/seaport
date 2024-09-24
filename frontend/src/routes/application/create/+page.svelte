<script lang="ts" context="module">
  type FieldDataType = (typeof FieldDataType)[keyof typeof FieldDataType];
  const FieldDataType = {
    General: 'GENERAL',
    Image: 'IMAGE',
    Service: 'SERVICE'
  } as const;

  type FieldData =
    | {
        type: typeof FieldDataType.General;
        content: GeneralApplicationDto;
      }
    | {
        type: typeof FieldDataType.Image;
        content: Image;
      }
    | {
        type: typeof FieldDataType.Service;
        content: DeploymentDto;
      };
</script>

<script lang="ts">
  import AccordionContent, { type DeleteEvent } from '$lib/components/AccordionContent.svelte';
  import Input from '$lib/components/Input.svelte';
  import PlusCircleOutline from 'svelte-material-icons/PlusCircleOutline.svelte';
  import AccordionSection, { type Field } from '$lib/components/AccordionSection.svelte';
  import type { DeploymentDto, GeneralApplicationDto, Image } from '$lib/types/baseImageRequest';
  import ImageForm from '$lib/components/ImageForm.svelte';
  import ImageBaseForm from '$lib/components/ImageBaseForm.svelte';
  import ImageArgumentForm from '$lib/components/ImageArgumentForm.svelte';
  import { ImageType } from '$lib/types/baseImage';
  import ServiceForm from '$lib/components/ServiceForm.svelte';
  import ServicePropertiesForm from '$lib/components/ServicePropertiesForm.svelte';
  import Select from '$lib/components/Select.svelte';
  import { goto } from '$app/navigation';
  import { createApplication } from '$lib/service/applicationService';
  import { MessageType, wsConnectTest } from '$lib/service/websocketService';
  import { onDestroy } from 'svelte';
  import Loader from '$lib/components/Loader.svelte';

  const DEFAULT_FIELD_NAME = 'Default';

  const defaultImage: Image = {
    uid: crypto.randomUUID(),
    type: 'GIT',
    name: 'Default',
    base: {
      language: '',
      version: ''
    },
    data: {
      name: '',
      version: ''
    },
    buildTool: '',
    version: '',
    buildArgs: {}
  };

  const defaultService: DeploymentDto = {
    image: defaultImage.uid,
    name: '',
    port: 8080,
    service: { port: 80, name: '' },
    ingress: { path: '' },
    environment: { values: {} }
  };

  const defaultGeneralSection: GeneralApplicationDto = {
    name: '',
    namespace: 'vangel'
  };

  let sectionName: string;
  let fields: Field<FieldData>[] = [
    createGeneralSectionField(),
    createImageField(DEFAULT_FIELD_NAME, defaultImage),
    createServiceField(DEFAULT_FIELD_NAME, defaultService)
  ];

  function createGeneralSectionField(): Field<FieldData> {
    return {
      id: `general`,
      title: `General`,
      intoView: false,
      deletable: false,
      data: {
        type: FieldDataType.General,
        content: defaultGeneralSection
      },
      subFields: []
    };
  }

  function createImageField(name: string, image: Image): Field<FieldData> {
    return {
      id: `base-${image.uid}`,
      title: `Application - ${name}`,
      intoView: false,
      deletable: name !== 'Default',
      data: {
        type: FieldDataType.Image,
        content: image
      },
      subFields: [
        {
          id: `base-build-${image.uid}`,
          title: 'Build',
          intoView: false,
          deletable: false,
          data: {
            type: FieldDataType.Image,
            content: image
          }
        },
        {
          id: `base-properties-${image.uid}`,
          title: 'Properties',
          intoView: false,
          deletable: false,
          data: {
            type: FieldDataType.Image,
            content: image
          }
        }
      ]
    };
  }

  function createServiceField(name: string, service: DeploymentDto) {
    const partialId = name.replaceAll(/\s+/g, '-').toLocaleLowerCase();
    return {
      id: `application-${partialId}`,
      title: `Service - ${name}`,
      intoView: false,
      deletable: name !== DEFAULT_FIELD_NAME,
      data: {
        type: FieldDataType.Service,
        content: service
      },
      subFields: [
        {
          id: `application-properties-${partialId}`,
          title: 'Properties',
          intoView: false,
          deletable: false,
          data: {
            type: FieldDataType.Service,
            content: service
          }
        }
      ]
    };
  }

  function handleDelete({ detail: deleteData }: CustomEvent<DeleteEvent>) {
    fields = fields.filter(field => field.id !== deleteData.id);
  }

  function createImage(name: string): Image {
    return {
      uid: crypto.randomUUID(),
      type: ImageType.Exe,
      name: name,
      base: {
        language: '',
        version: ''
      },
      data: {
        name: '',
        version: ''
      },
      file: undefined,
      buildArgs: {}
    };
  }

  function createService(): DeploymentDto {
    return {
      image: defaultImage.uid,
      name: '',
      port: 8080,
      service: { port: 80, name: '' },
      ingress: { path: '' },
      environment: { values: {} }
    };
  }

  function addBaseImage(name: string) {
    if (name === DEFAULT_FIELD_NAME) {
      return;
    }

    fields = [...fields, createImageField(name, createImage(name))];
    sectionName = '';
  }

  function addService(name: string) {
    if (name === DEFAULT_FIELD_NAME) {
      return;
    }

    fields = [...fields, createServiceField(name, createService())];
    sectionName = '';
  }

  let ws: WebSocket | undefined;
  let loading = false;
  async function handleCreate() {
    loading = true;
    ws = handleSocketConnection();
  }

  onDestroy(() => {
    ws?.close();
  });

  async function create(socket: string) {
    await createApplication(socket, {
      request: {
        ...extractGeneral(),
        deployments: extractServices()
      },
      images
    }).finally(() => {
      loading = false;
      goHome();
    });
  }

  let state: string;
  function handleSocketConnection(): WebSocket {
    return wsConnectTest(message => {
      if (message.type === MessageType.Identifier) {
        create(message.id);
      } else if (message.type === MessageType.String) {
        state = message.data;
      }
    });
  }

  function goHome() {
    goto('/', { replaceState: true });
  }

  function extractServices() {
    return fields.filter(s => s.data.type === FieldDataType.Service).map(s => s.data.content as DeploymentDto);
  }

  function extractGeneral() {
    return fields.find(s => s.data.type === FieldDataType.General)?.data.content as GeneralApplicationDto;
  }

  $: images = fields
    .filter(s => s.data.type === FieldDataType.Image)
    .map(s => s.data.content as Image)
    .reduce(
      (acc, image) => ({
        ...acc,
        [image.uid]: image
      }),
      {} as Record<string, Image>
    );
</script>

<form class="h-full w-full" on:submit|preventDefault={handleCreate}>
  <AccordionContent {fields} on:delete={handleDelete}>
    <svelte:fragment slot="sidebar-actions">
      <div class="grid grid-cols-1 gap-3 py-4">
        <form class="grid gap-2">
          <Input id="add-app" name="add-app" bind:value={sectionName}>Name</Input>
          <button
            on:click={() => addBaseImage(sectionName)}
            type="button"
            class="primary flex w-full min-w-fit items-center gap-2"
            disabled={!sectionName}>
            <span><PlusCircleOutline class="" height="1.2rem" width="1.2rem" /></span>
            <span>Application</span>
          </button>
          <button
            on:click={() => addService(sectionName)}
            type="button"
            class="primary flex min-w-fit items-center gap-2"
            disabled={!sectionName}>
            <span><PlusCircleOutline class="" height="1.2rem" width="1.2rem" /></span>
            <span>Service</span>
          </button>
        </form>
      </div>
    </svelte:fragment>

    <svelte:fragment slot="sidebar-end">
      {#if loading}
        <div class="px-4 py-2 text-sm font-bold opacity-40">
          <span class="px-2 text-lg"><Loader /></span>
          {#if state}
            {state}
          {/if}
        </div>
      {/if}
      <div class="grid grid-cols-2 gap-4 px-4">
        <button class="primary" disabled={loading} type="submit">Save</button>
        <button class="secondary" disabled={loading} type="button" on:click={goHome}>Cancel</button>
      </div>
    </svelte:fragment>
    <svelte:fragment slot="complete-content" let:fields>
      {#each fields as field, i}
        <AccordionSection bind:field>
          <svelte:fragment slot="content" let:id>
            {#if id.startsWith('general') && field.data.type === FieldDataType.General}
              <div class="grid gap-4 py-2">
                <Input id="{id}-collectoin-name" name="{id}-collection-name" bind:value={field.data.content.name}>
                  Collection name
                </Input>
                <Select
                  id="{id}-namespace"
                  name="{id}-namespace"
                  bind:value={field.data.content.namespace}
                  options={[{ name: 'vangel', value: 'vangel' }]}>
                  Group
                </Select>
              </div>
            {:else if id.startsWith('base') && field.data.type === FieldDataType.Image}
              <ImageForm bind:image={field.data.content} />
            {:else if id.startsWith('application') && field.data.type === FieldDataType.Service}
              <ServiceForm id={field.id} bind:deployment={field.data.content} {images} />
            {/if}
          </svelte:fragment>

          <svelte:fragment slot="sub-content" let:id let:subId>
            {#if id.startsWith('base') && field.data.type === FieldDataType.Image}
              {#if subId.startsWith('base-build')}
                <ImageBaseForm bind:image={field.data.content} />
              {:else if subId.startsWith('base-properties')}
                <ImageArgumentForm bind:image={field.data.content} />
              {/if}
            {/if}

            {#if id.startsWith('application') && field.data.type === FieldDataType.Service}
              {#if subId.startsWith('application-properties')}
                <ServicePropertiesForm bind:deployment={field.data.content} {images} />
              {/if}
            {/if}
          </svelte:fragment>
        </AccordionSection>
      {/each}
    </svelte:fragment>
  </AccordionContent>
</form>
