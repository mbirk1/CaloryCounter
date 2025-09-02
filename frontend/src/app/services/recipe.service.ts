import { RecipeModel } from '../models/RecipeModel'
import { Injectable, resource } from '@angular/core'
import { environment } from '../../environment/environment'

@Injectable({
  providedIn: 'root',
})
export class RecipeService {
  apiUrl = environment.apiBaseUrl

  private recipeResource = resource<RecipeModel[], unknown>({
    loader: async (params) => {
      const response = await fetch(this.apiUrl + '/recipe')
      if (!response.ok) {
        throw new Error('Failed to fetch foods')
      }
      return await response.json()
    },
  })

  reload() {
    this.recipeResource.reload()
  }

  getRecipe(): RecipeModel[] {
    return this.recipeResource.value() ?? []
  }
}
