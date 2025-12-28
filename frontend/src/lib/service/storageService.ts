import { configuration } from '$lib/client/config';
import { StorageApi, type Pageable } from '$lib/generated';

const storageApi = new StorageApi(configuration);

export type StorageCriteriaFilter = {
  name?: string;
};

export const getStoredResources = async (criteriaFilter?: StorageCriteriaFilter, pageable?: Pageable) => {
  return await storageApi.getStoredResources({
    name: criteriaFilter?.name,
    page: pageable?.page,
    size: pageable?.size,
    sort: pageable?.sort
  });
};
