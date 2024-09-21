import { ImageType, type BuildArg } from '$lib/types/baseImage';
import type { Image } from '$lib/types/baseImageRequest';

const BASE_URL = 'http://localhost:8080/api/v1';
const BASE_IMAGE_API = 'base-image';

type ImageArgResponse = {
  arguments: BuildArg[];
};

export async function fetchLanguages() {
  return (await fetch(`${BASE_URL}/${BASE_IMAGE_API}/languages`)
    .then(t => t.json())
    .catch(e => catchAndDefault(e, []))) as string[];
}

export async function fetchVersions(language: string | undefined) {
  if (!language) {
    return [];
  }

  return (await fetch(`${BASE_URL}/${BASE_IMAGE_API}/language/${language}/versions`)
    .then(t => t.json())
    .catch(e => catchAndDefault(e, []))) as string[];
}

export async function fetchBuildTools(language: string | undefined, version: string | undefined) {
  if (!language || !version) {
    return [];
  }

  const params = new URLSearchParams({ language, version });

  return (await fetch(`${BASE_URL}/${BASE_IMAGE_API}/build-tools?${encodeURI(params.toString())}`)
    .then(t => t.json())
    .catch(e => catchAndDefault(e, []))) as string[];
}

export async function fetchBuildToolVersions(buildTool: string | undefined) {
  if (!buildTool) {
    return [];
  }

  return (await fetch(`${BASE_URL}/${BASE_IMAGE_API}/build-tool/${buildTool}/versions`)
    .then(t => t.json())
    .catch(e => catchAndDefault(e, []))) as string[];
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

  const params = new URLSearchParams({
    type,
    language: language,
    languageVersion: languageVersion,
    'buildTool.name': buildTool ?? '',
    'buildTool.version': buildToolVersion ?? ''
  });

  return await fetch(`${BASE_URL}/${BASE_IMAGE_API}/build-arguments?${params}`)
    .then(t => t.json())
    .then((r: ImageArgResponse) => r.arguments)
    .catch(e => {
      console.error(e);
      return [];
    });
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