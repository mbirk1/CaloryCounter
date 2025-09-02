import { environment } from './environment.development'

export const API_ENDPOINTS = {
  food: `${environment.apiBaseUrl}/food`,
  recipe: `${environment.apiBaseUrl}/recipe`,
}
