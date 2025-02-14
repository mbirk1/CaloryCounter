import { Injectable, resource } from '@angular/core'
import { FoodModel } from '../models/FoodModel'
import { environment } from '../../environment/environment'

@Injectable({
  providedIn: 'root',
})
export class FoodService {
  apiUrl = environment.apiBaseUrl

  private foodResource = resource<FoodModel[], unknown>({
    loader: async (params) => {
      const response = await fetch(this.apiUrl + '/food')
      if (!response.ok) {
        throw new Error('Failed to fetch foods')
      }
      return await response.json()
    },
  })

  reload() {
    this.foodResource.reload()
  }

  getFood(): FoodModel[] {
    return this.foodResource.value() ?? []
  }
}
