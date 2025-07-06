import { BASE_URL } from '$lib/config';
import { ImageApi, type ImagesResponse, type Pageable, type ImageDetails, type ImageLog } from '$lib/generated';
import { ImageType } from '$lib/types/baseImage';
import { type Image as ImageRequest } from '$lib/types/baseImageRequest';
import { configuration } from '$lib/client/config';

const BASE_IMAGE_URL = `${BASE_URL}/v2/images`;

const imageApi = new ImageApi(configuration);

export type ImageCreateRequest = {
  name: string;
};

export async function getImages(pageable?: Pageable): Promise<ImagesResponse> {
  return await imageApi.getImages({
    page: pageable?.page,
    size: pageable?.size,
    sort: pageable?.sort
  });
}

export async function getImageLogs(imageId: number): Promise<ImageLog> {
  return imageApi.getImageLog({ id: imageId });
}

export async function getImage(id: number): Promise<ImageDetails> {
  return imageApi.getImage({ id });
}

export async function createImage(request: ImageCreateRequest) {
  return imageApi.createImage({ imageCreateRequest: request });
}

export async function editImage(id: number, request: ImageCreateRequest) {
  return imageApi.editImage({
    id,
    imageUpdateRequest: request
  });
}

export async function deleteImage(id: number) {
  return await imageApi.deleteImage({ id });
}

export async function releaseImageTag(id: number, request: ImageRequest, socket?: string) {
  if (request.type === 'EXE') {
    const body = new FormData();

    if (!request.file) return;

    body.append('files', request.file, request.uid);
    body.append('body', `${imageToJson(request, socket)}`);
    return await fetch(`${BASE_IMAGE_URL}/${id}/exe/create`, {
      method: 'POST',
      body: body
    });
  } else {
    return await fetch(`${BASE_IMAGE_URL}/${id}/git/create`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: imageToJson(request, socket)
    });
  }
}

function imageToJson(image: ImageRequest, socket?: string) {
  const base = {
    socket,
    data: {
      name: image.data.name,
      version: image.data.version
    },
    base: {
      version: image.base.version,
      language: image.base.language
    },
    buildArgs: image.buildArgs
  };
  switch (image.type) {
    case ImageType.Git:
      return JSON.stringify({
        ...base,
        buildTool: image.buildTool,
        version: image.version,
        namespace: 'removed'
      });
    case ImageType.Exe:
      return JSON.stringify(base);
  }
}
