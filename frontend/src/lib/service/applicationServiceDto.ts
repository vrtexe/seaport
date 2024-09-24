export type NamespaceDto = {
  uid: string;
  name: string;
  applications: ApplicationDto[];
  externalService: ExternalServiceDto[];
};

export type ExternalServiceDto = {
  id: number;
};

export type ApplicationDto = {
  id: number;
  name: string;
  images: ImageDto[];
  deployments: DeploymentDto[];
  baseImages: BaseImageDto[];
};

export type BaseImageGitDto = {
  buildTool: string;
  buildToolVersion: string;
};

export type BaseImageDto = {
  id: number;
  language: string;
  languageVersion?: string;
} & (({ type: typeof ImageTypeDto.Git } & BaseImageGitDto) | { type: typeof ImageTypeDto.Exe });

export type ImageTypeDto = (typeof ImageTypeDto)[keyof typeof ImageTypeDto];
export const ImageTypeDto = {
  Git: 'GIT',
  Exe: 'EXE'
} as const;

export type DeploymentDto = {
  id: number;
  name: string;
  pods: PodDto[];
  ingresses: IngressDto[];
};

export type ServiceDto = {
  id: number;
  name: string;
  port: ServicePortDto[];
};

export type ServicePortDto = {
  name: string;
  port: number;
  targetPort: number;
};

export type IngressDto = {
  id: number;
  ingressRules: IngressRuleDto[];
};

export type IngressRuleDto = {
  path: string;
  service: ServiceDto;
};

export type PodDto = {
  id: number;
  name: string;
  port: number;
  image: ImageDto;
  environment: EnvironmentDto;
};

export type ImageDto = {
  id: number;
  name: string;
  version: string;
  hash: string;
  baseRef: number;
  arguments: Record<string, string>;
};

export type EnvironmentDto = {
  id: number;
  name: string;
  content: Record<string, string>;
};
