export type UserRole = (typeof UserRole)[keyof typeof UserRole];
export const UserRole = Object.freeze({
  USER: 'USER',
  ADMIN: 'ADMIN'
} as const);

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  userRoles: UserRole[];
  username: string;
}

export const DEFAULT_USER_ROLES: UserRole[] = [];
