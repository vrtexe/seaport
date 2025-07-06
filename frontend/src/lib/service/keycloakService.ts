import { DEFAULT_USER_ROLES, type User, type UserRole } from '$lib/model/user';
import type { KeycloakConfig, KeycloakRoles, KeycloakTokenParsed } from 'keycloak-js';
import Keycloak from 'keycloak-js';
import { writable } from 'svelte/store';

export const user = writable<User | undefined>();

let keycloak: Keycloak | undefined;
let authenticationToken: string | undefined;

const keycloakConfig: KeycloakConfig = {
  url: 'http://localhost/sso',
  realm: 'main',
  clientId: 'seaport'
};

export const initKeycloak = async () => {
  const authenticated = await setupKeycloak();
  if (!authenticated) {
    await keycloak?.login();
  }

  user.set(loadUserFromKeycloak());
  authenticationToken = loadKeycloakToken();
};

export const setupKeycloak = async () => {
  keycloak = await createKeycloakInstance();

  keycloak.onTokenExpired = () => {
    keycloak
      ?.updateToken(60)
      .then(refreshed => {
        if (!refreshed) return;
        updateToken(keycloak?.token);
      })
      .catch(err => {
        console.error('Failed to refresh the token, or the session has expired', err);
        keycloak?.clearToken();
        keycloak?.logout();
      });
  };

  return keycloak.init();
};

async function createKeycloakInstance() {
  return new Keycloak(keycloakConfig);
}

const updateToken = (token: string | undefined) => {
  authenticationToken = token;
};

export const getAuthenticationToken = () => {
  return authenticationToken;
};

export const login = async () => {
  return await keycloak?.login();
};

export const register = async () => {
  return await keycloak?.register();
};

export const logout = async () => {
  return await keycloak?.logout();
};

const loadKeycloakToken = () => {
  if (!keycloak) return;
  return keycloak.token;
};

const loadUserFromKeycloak = (): User | undefined => {
  if (!keycloak) return;
  const idTokenParsed = keycloak.idTokenParsed as KeycloakTokenParsed;
  const tokenParsed = keycloak.tokenParsed;

  return {
    id: keycloak.subject ? keycloak.subject : '',
    email: idTokenParsed.email,
    firstName: idTokenParsed.given_name,
    lastName: idTokenParsed.family_name,
    username: idTokenParsed.preferred_username,
    userRoles: extractUserRoles(tokenParsed)
  };
};

function extractUserRoles(tokenParsed: KeycloakTokenParsed | undefined): UserRole[] {
  const keycloakRoles: KeycloakRoles | undefined = tokenParsed?.realm_access;
  const roles: string[] | undefined = keycloakRoles?.roles;

  const rolesSet = new Set(roles ? [...roles] : DEFAULT_USER_ROLES);

  return Array.from(rolesSet) as UserRole[];
}
