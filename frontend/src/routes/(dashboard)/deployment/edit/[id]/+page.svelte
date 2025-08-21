<script lang="ts">
  import { page } from '$app/stores';
  import DeploymentCreate, { DialogType } from '$lib/components/pages/dashboard/deployment/DeploymentCreate.svelte';
  import type { Deployment, DeploymentDetails } from '$lib/generated';
  import { getDeployment } from '$lib/service/deploymentService';
  import { onMount } from 'svelte';

  let deployment: DeploymentDetails | undefined;
  let id: number | undefined;
  onMount(() => {
    id = parseInt($page.params['id']);
    loadData(id);
  });

  async function loadData(id: number) {
    deployment = await getDeployment(id);
  }
</script>

{#if deployment}
  <DeploymentCreate
    type={DialogType.Edit}
    {id}
    data={{
      deployment: { name: deployment.general.name, port: deployment.general.port },
      imageTag: deployment.imageTag,
      service: {
        name: deployment.service.name
      },
      group: deployment.group,
      ingress: {
        enabled: Boolean(deployment.ingress),
        name: deployment.ingress?.name ?? '',
        path: deployment.ingress?.path ?? ''
      },
      environment: {
        values: Object.entries(deployment.environment).map(([k, v]) => ({ name: k, value: v }))
      }
    }} />
{/if}
