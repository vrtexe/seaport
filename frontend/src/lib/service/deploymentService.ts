import { DeploymentApi, type DeploymentCreateRequest, type DeploymentsResponse } from '$lib/generated';
import { configuration } from '$lib/client/config';

const deploymentApi = new DeploymentApi(configuration);

export async function getAllDeployments(): Promise<DeploymentsResponse> {
  return await deploymentApi.getDeployments({
    // page: pageable.page,
    // size: pageable.size,
    // sort: pageable.sort
  });
}

// export async function getDeployment(id: number): Promise<ImageDetails1> {
//   return deploymentApi.getDeployments({ id });
// }

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
