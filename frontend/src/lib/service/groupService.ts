import { BASE_URL } from '$lib/config';
import {
  Configuration,
  GroupApi,
  type Group,
  type GroupCreateRequest,
  type GroupsResponse,
  type GroupUpdateRequest,
  type Pageable
} from '$lib/generated';

const configuration = new Configuration({
  basePath: BASE_URL
});

const groupApi = new GroupApi(configuration);

export async function getAllGroups(pageable: Pageable): Promise<GroupsResponse> {
  return await groupApi.getAllGroups({
    page: pageable.page,
    size: pageable.size,
    sort: pageable.sort
  });
}

export async function createGroup(request: GroupCreateRequest): Promise<Group> {
  return await groupApi.createGroup({
    groupCreateRequest: request
  });
}

export async function updateGroup(id: number, request: GroupUpdateRequest): Promise<Group> {
  return await groupApi.updateGroup({
    id,
    groupUpdateRequest: request
  });
}

export async function deleteGroup(id: number) {
  return await groupApi.deleteGroup({ id });
}
