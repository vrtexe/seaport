import type { Middleware, ResponseContext } from '$lib/generated';
import { Header } from '$lib/model/header';
import { awaitKeycloak, getAuthenticationToken } from '$lib/service/keycloakService';

export const authenticationMiddleware = (): Middleware => ({
  async pre(context: ResponseContext) {
    await awaitKeycloak();
    return {
      ...context,
      init: {
        ...context.init,
        headers: {
          ...context.init.headers,
          [Header.Authorization]: buildBearerToken()
        }
      }
    };
  }
});

const buildBearerToken = () => `Bearer ${getAuthenticationToken()}`;
