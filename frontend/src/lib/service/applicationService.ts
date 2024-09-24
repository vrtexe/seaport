import type { NamespaceDto } from '$lib/service/applicationServiceDto';
import { ImageType } from '$lib/types/baseImage';
import type {
  ApplicationRequest,
  Image,
  ApplicationDto,
  DeploymentDto,
  EnvironmentDto
} from '$lib/types/baseImageRequest';

const BASE_URL = 'http://localhost:8080/api/v1';
const IMAGE_API = 'image';
const APPLICATION_API = 'application';

type TypedApplicationRequest = { type: ImageType } & ApplicationRequest;

export async function createApplication(socket: string, application: ApplicationRequest) {
  const promises: Promise<unknown>[] = [];
  for (const request of splitRequest(application)) {
    promises.push(createSingleApplication(socket, request));
  }

  return await Promise.all(promises);
}

export function startDeployment(id: number) {
  fetch(`${BASE_URL}/${APPLICATION_API}/deployment/${id}/start`);
}

export async function updateDeployment(id: number, version: string) {
  fetch(`${BASE_URL}/${APPLICATION_API}/deployment/${id}/update?version=${version}`);
}

export async function editDeployment(id: number, properties: Record<string, string>) {
  fetch(`${BASE_URL}/${APPLICATION_API}/deployment/${id}/edit`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      properties: properties
    })
  });
}

export function stopDeployment(id: number) {
  fetch(`${BASE_URL}/${APPLICATION_API}/deployment/${id}/stop`);
}

export async function getApplications(namespace: string) {
  return (await fetch(`${BASE_URL}/${APPLICATION_API}?namespace=${namespace}`)
    .then(res => res.json())
    .catch(e => catchAndDefault(e, []))) as NamespaceDto;
}

function splitRequest(application: ApplicationRequest): TypedApplicationRequest[] {
  const gitRequest: TypedApplicationRequest = {
    type: ImageType.Git,
    request: {
      name: application.request.name,
      namespace: application.request.namespace,
      deployments: []
    },
    images: {}
  };
  const exeRequest: TypedApplicationRequest = {
    type: ImageType.Exe,
    request: {
      name: application.request.name,
      namespace: application.request.namespace,
      deployments: []
    },
    images: {}
  };

  for (const deployment of application.request.deployments) {
    const image = application.images[deployment.image];
    switch (image.type) {
      case ImageType.Exe: {
        exeRequest.images[image.uid] = image;
        exeRequest.request.deployments.push(deployment);
        break;
      }

      case ImageType.Git: {
        gitRequest.images[image.uid] = image;
        gitRequest.request.deployments.push(deployment);
        break;
      }
    }
  }

  const response: TypedApplicationRequest[] = [];

  if (Object.keys(gitRequest.images).length > 0) response.push(gitRequest);
  if (Object.keys(exeRequest.images).length > 0) response.push(exeRequest);

  return response;
}

export async function createSingleApplication(socket: string, application: ApplicationRequest) {
  if (Object.values(application.images).some(s => s.type === 'EXE')) {
    const body = new FormData();

    for (const image of Object.values(application.images)) {
      if (image.type === ImageType.Exe && image.file) {
        body.append('files', image.file, image.uid);
      }
    }

    body.append('body', applicationRequestToJson(socket, application.request));
    body.append('request', imageRequestToJson(application.images, application.request));
    return await fetch(`${BASE_URL}/${IMAGE_API}/exe/create`, {
      method: 'POST',
      body: body
    });
  } else {
    return await fetch(`${BASE_URL}/${IMAGE_API}/git/create`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: `{
        "request": ${applicationRequestToJson(socket, application.request)},
        "images": {
          ${completeImagesToJson(application.images, application.request)}
        }
      }`
    });
  }
}

function applicationRequestToJson(socket: string, application: ApplicationDto) {
  return `{
    "socket": "${socket}",
    "name": "${application.name}",
    "namespace": "${application.namespace}",
    "deployments": [
        ${deploymentsToJson(application)}
    ]
 }`;
}

function deploymentsToJson(application: ApplicationDto) {
  return application.deployments.map(deployment => deploymentRequestToJson(deployment, application)).join(',');
}

function deploymentRequestToJson(deployment: DeploymentDto, application: ApplicationDto) {
  return `{
             "port": "${deployment.port}",
             "name": "${deployment.name}",
             "image": "${deployment.image}",
             "service": {
                 "port": "${deployment.service.port}"
             },
             "ingress": {
                 "path": "${application.namespace}/${application.name}/${deployment.ingress.path}"
             },
             "environment": ${environmentValuesToJson(deployment.environment)}
         }
  `;
}

function environmentValuesToJson(environment: EnvironmentDto) {
  return `{
    "values": ${JSON.stringify(environment.values)}
  }`;
}

function imageRequestToJson(images: Record<string, Image>, application: ApplicationDto) {
  return `{
     "namespace": "${application.namespace}",
     "images": {
         ${imagesToJson(images)}
     }
 }`;
}

function imagesToJson(images: Record<string, Image>) {
  return Object.values(images).map(imageToJson).join(',');
}

function imageToJson(image: Image) {
  return `"${image.uid}": {
              "uid": "${image.uid}",
              "data": {
                "name": "${image.data.name}",
                "version": "${image.data.version}"
              },
              "base": {
                "language": "${image.base.language}",
                "version": "${image.base.version}"
              },
              "buildArgs": ${imageBuildArgsToJson(image)}
         }`;
}

function imageBuildArgsToJson(image: Image) {
  return JSON.stringify(image.buildArgs);
}

function completeImagesToJson(images: Record<string, Image>, application: ApplicationDto) {
  return Object.values(images)
    .map(image => completeImageToJson(image, application))
    .join(',');
}

function completeImageToJson(image: Image, application: ApplicationDto) {
  return `"${image.uid}": {
              "type": "${image.type}",
              "uid": "${image.uid}",
              "namespace": "${application.namespace}",
              "data": {
                "name": "${image.data.name}",
                "version": "${image.data.version}"
              },
              "base": {
                "language": "${image.base.language}",
                "version": "${image.base.version}"
              },
              "buildArgs": ${imageBuildArgsToJson(image)}
              ${gitPropertiesToJson(image)}
         }`;
}

function gitPropertiesToJson(image: Image) {
  return image.type === ImageType.Git
    ? `,
      "buildTool": "${image.buildTool}",
      "version": "${image.version}"
    `
    : '';
}

function catchAndDefault<T>(e: unknown, defaultValue: T): T {
  console.error(e);
  return defaultValue;
}
