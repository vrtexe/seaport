export type ImageType = (typeof ImageType)[keyof typeof ImageType];
export const ImageType = {
  Git: 'GIT',
  Exe: 'EXE'
} as const;

export type BuildArgType = (typeof BuildArgType)[keyof typeof BuildArgType];
export const BuildArgType = {
  String: 'STRING',
  File: 'FILE'
} as const;

export type BuildArgStage = (typeof BuildArgStage)[keyof typeof BuildArgStage];
export const BuildArgStage = {
  Build: 'BUILD',
  Runtime: 'RUNTIME'
} as const;

export type BaseBuildArg = {
  name: string;
  description: string;
  stage: BuildArgStage;
};

export type BuildArg = BaseBuildArg &
  (
    | {
        type: typeof BuildArgType.String;
        value: string;
      }
    | {
        type: typeof BuildArgType.File;
        value: FileList;
      }
  );
