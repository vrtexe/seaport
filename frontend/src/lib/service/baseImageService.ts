import { ImageType, type BuildArg } from '$lib/types/baseImage';
import type { Image } from '$lib/types/baseImageRequest';
// import { BASE_URL } from '$lib/config';
import { BaseImageApi, type BaseImageRequest, type BaseImageType, type Pageable } from '$lib/generated';
import { configuration } from '$lib/client/config';

// const BASE_URL = 'http://localhost:8081/api/v1'
// const BASE_URL = 'http://localhost/app/api/v1';
// const BASE_IMAGE_API = 'v1/base-image';

const baseImageApi = new BaseImageApi(configuration);

// type ImageArgResponse = {
// arguments: BuildArg[];
// };

export type BaseImageFilter = {
  buildTool?: string | undefined;
  language?: string | undefined;
  type?: BaseImageType | undefined;
};

export async function getBaseImage(id: number) {
  return await baseImageApi.getBaseImage({ id });
}

export async function getBaseImages(filter?: BaseImageFilter, pageable?: Pageable) {
  return await baseImageApi.getBaseImages({
    ...(filter ?? {}),
    ...(pageable ?? {})
  });
}

export async function createBaseImage(update: BaseImageRequest) {
  return await baseImageApi.createBaseImages({ baseImageRequest: update });
}

export async function updateBaseImage(id: number, update: BaseImageRequest) {
  return await baseImageApi.updateBaseImages({ id, baseImageRequest: update });
}

export async function deleteBaseImage(id: number) {
  return await baseImageApi.deleteBaseImages({ id });
}

export async function fetchLanguages() {
  return await baseImageApi.getBaseImageLanguages().catch(handleErrorWithDefault);
  // return (await fetch(`${BASE_URL}/${BASE_IMAGE_API}/languages`)
  //   .then(t => t.json())
  //   .catch(e => catchAndDefault(e, []))) as string[];
}

export async function fetchVersions(language: string | undefined) {
  if (!language) {
    return [];
  }
  return await baseImageApi.getBaseImageLanguageVersions({ name: language }).catch(handleErrorWithDefault);

  // return (await fetch(`${BASE_URL}/${BASE_IMAGE_API}/language/${language}/versions`)
  //   .then(t => t.json())
  //   .catch(e => catchAndDefault(e, []))) as string[];
}

export async function fetchBuildTools(language: string | undefined, version: string | undefined) {

  // const params = new URLSearchParams({ language, version });
  return await baseImageApi.getBaseImageBuildTools({ language, version }).catch(handleErrorWithDefault);

  // return (await fetch(`${BASE_URL}/${BASE_IMAGE_API}/build-tools?${encodeURI(params.toString())}`)
  // .then(t => t.json())
  // .catch(e => catchAndDefault(e, []))) as string[];
}

export async function fetchBuildToolVersions(buildTool: string | undefined) {
  if (!buildTool) {
    return [];
  }

  return await baseImageApi.getBaseImageBuildToolVersions({ name: buildTool }).catch(handleErrorWithDefault);

  // return (await fetch(`${BASE_URL}/${BASE_IMAGE_API}/build-tool/${buildTool}/versions`)
  // .then(t => t.json())
  // .catch(e => catchAndDefault(e, []))) as string[];
}

export async function fetchImageArgs(
  type: ImageType,
  language: string | undefined,
  languageVersion: string | undefined,
  buildTool: string | undefined,
  buildToolVersion: string | undefined
) {
  if (!language || !languageVersion || (type === ImageType.Git && (!buildTool || !buildToolVersion))) {
    return [];
  }

  // const params = new URLSearchParams({
  //   type,
  //   language: language,
  //   languageVersion: languageVersion,
  //   'buildTool.name': buildTool ?? '',
  //   'buildTool.version': buildToolVersion ?? ''
  // });

  return await baseImageApi
    .getBaseImageBuildArguments({
      type,
      language,
      languageVersion,
      buildToolName: buildTool,
      buildToolVersion: buildToolVersion
    })
    .then(r =>
      r.map(
        a =>
          <BuildArg>{
            description: a.description,
            name: a.name,
            stage: a.stage,
            type: a.type
          }
      )
    )
    .catch(handleErrorWithDefault);

  // return await fetch(`${BASE_URL}/${BASE_IMAGE_API}/build-arguments?${params}`)
  //   .then(t => t.json())
  //   .then((r: ImageArgResponse) => r.arguments)
  //   .catch(e => {
  //     console.error(e);
  //     return [];
  //   });
}

export async function fetchImageArgsFromDto(image: Image) {
  return fetchImageArgs(
    image.type,
    image.base.language,
    image.base.version,
    image.type === ImageType.Git ? image.buildTool : undefined,
    image.type === ImageType.Git ? image.version : undefined
  );
}

function catchAndDefault<T>(e: unknown, defaultValue: T): T {
  console.error(e);
  return defaultValue;
}

function handleErrorWithDefault<T extends []>(e: unknown): T {
  return catchAndDefault<T>(e, [] as T);
}
