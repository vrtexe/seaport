import { NamespaceApi } from '$lib/generated';
import { configuration } from '$lib/client/config';

const namespaceApi = new NamespaceApi(configuration);

export const getUserNamespace = async () => {
  return await namespaceApi.getNamespace();
};
