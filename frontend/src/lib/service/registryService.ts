import { RegistryApi, type ImagesResponse, type Pageable, type ImageDetails, type ImageLog } from '$lib/generated';
import { configuration } from '$lib/client/config';

const registryApi = new RegistryApi(configuration);

export type RegistryImageQuery = {
  user?: string;
  app?: string;
  namespace?: string;
};

export async function getRegistryImages(query?: RegistryImageQuery, pageable?: Pageable): Promise<ImagesResponse> {
  return registryApi.getRegistryImages({
    page: pageable?.page,
    size: pageable?.size,
    sort: pageable?.sort,

    user: query?.user,
    app: query?.app,
    namespace: query?.namespace
  });
}

export async function getRegistryImage(id: number): Promise<ImageDetails> {
  return registryApi.getRegistryImage({ id });
}

export async function getRegistryImageLog(id: number): Promise<ImageLog> {
  return registryApi.getRegistryImageLog({ id });
}

export async function deleteRegistryImage(id: number): Promise<void> {
  return registryApi.deleteRegistryImage({ id });
}

export async function deleteRegistryImageTag(id: number): Promise<void> {
  return registryApi.deleteRegistryImageTag({ id });
}
