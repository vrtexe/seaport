import type { ImageType } from '$lib/types/baseImage';

export type ApplicationRequest = {
  request: ApplicationDto;
  images: Record<string, Image>;
};

export type ImageNameProp = { name: string };

export type Image = ImageNameProp & ImageBaseData & (GitImageData | ExeImageData);

export type ImageBaseData = {
  uid: string;
  data: ImageDataDto;
  base: ImageParams;
  buildArgs: Record<string, string>;
};

export type GitImageData = {
  type: typeof ImageType.Git;
  buildTool: string;
  version: string;
};

export type ExeImageData = {
  type: typeof ImageType.Exe;
  file: File | undefined;
};

export type ImageParams = {
  language: string;
  version: string;
};

export type ImageDataDto = {
  name: string;
  version: string;
};

export type GeneralApplicationDto = {
  name: string;
  namespace: string;
};

export type ApplicationDto = GeneralApplicationDto & {
  deployments: DeploymentDto[];
};

export type DeploymentDto = {
  port: number;
  name: string;
  image: string;
  service: ServiceDto;
  ingress: IngressDto;
  environment: EnvironmentDto;
};

export type ServiceDto = {
  port: number;
  name: string;
};

export type IngressDto = {
  path: string;
};

export type EnvironmentDto = {
  values: Record<string, string>;
};


