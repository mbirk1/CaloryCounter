import { environment } from './environment'

export const API_ENDPOINTS = {
  food: `${environment.apiBaseUrl}/food`,
  recipe: `${environment.apiBaseUrl}/recipe`,
  auth: {
    register: `${environment.apiBaseUrl}/auth/register`,
    login: `${environment.apiBaseUrl}/auth/login`,
    refresh: `${environment.apiBaseUrl}/auth/refresh`,
    logout: `${environment.apiBaseUrl}/auth/logout`,
  },
  users: {
    me: `${environment.apiBaseUrl}/users/me`,
    profile: `${environment.apiBaseUrl}/users/me/profile`,
  },
}
