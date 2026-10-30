import { environment } from './environment'

export const API_ENDPOINTS = {
  food: `${environment.apiBaseUrl}/food`,
  foodImport: `${environment.apiBaseUrl}/food/import/bls`,
  recipe: `${environment.apiBaseUrl}/recipe`,
  foodDuplicates: `${environment.apiBaseUrl}/food/duplicates`,
  foodDuplicatesAnalyze: `${environment.apiBaseUrl}/food/duplicates/analyze`,
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
