import { login } from '$lib/service/keycloakService';

export const init = async () => {
  login();
  // await db.connect();
};
