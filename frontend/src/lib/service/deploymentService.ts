import {
  DeploymentApi,
  type DeploymentCreateRequest,
  type DeploymentDetails,
  type DeploymentsResponse,
  type DeploymentState,
  type Pageable
} from '$lib/generated';
import { configuration } from '$lib/client/config';

const deploymentApi = new DeploymentApi(configuration);

export async function getAllDeployments(group: string | undefined, pageable?: Pageable): Promise<DeploymentsResponse> {
  return await deploymentApi.getDeployments({
    group: group,
    page: pageable?.page,
    size: pageable?.size,
    sort: pageable?.sort
  });
}

export async function updateDeploymentState(id: number, state: DeploymentState) {
  return await deploymentApi.updateDeploymentState({ id, body: state });
}

export async function getDeployment(id: number): Promise<DeploymentDetails> {
  return deploymentApi.getDeployment({ id });
}

export async function createDeployment(request: DeploymentCreateRequest) {
  return await deploymentApi.createDeployment({ deploymentCreateRequest: request });
}

export async function deleteDeployment(id: number) {
  return await deploymentApi.deleteDeployment({ id });
}

export async function updateDeployment(id: number, request: DeploymentCreateRequest) {
  return await deploymentApi.updateDeployment({ id, deploymentCreateRequest: request });
}

// export async function editImage(id: number, request: DeploymentCreateRequest) {
//   return deploymentApi.updateDeployment({
//     id,

//     // : request
//   });
// }

export async function deleteImage(id: number) {
  deploymentApi.deleteDeployment({ id });
}
