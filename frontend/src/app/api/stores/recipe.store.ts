import {
  computed,
  inject,
  Injectable,
  resource,
  ResourceRef,
  Signal,
} from '@angular/core'
import { FoodModel } from '../../models/FoodModel'
import { Gateway } from '../gateways/gateway'
import { firstValueFrom } from 'rxjs'
import { API_ENDPOINTS } from '../../../environment/endpoints'
import { RecipeModel } from '../../models/RecipeModel'

@Injectable({
  providedIn: 'root',
})
export class RecipeStore {
  private readonly recipeGateway = inject<Gateway<RecipeModel>>(Gateway)

  recipeResource: ResourceRef<RecipeModel[] | undefined> = resource({
    loader: async (): Promise<RecipeModel[]> =>
      firstValueFrom(this.recipeGateway.get(API_ENDPOINTS.recipe)),
  })

  readonly recipes: Signal<RecipeModel[]> = computed((): RecipeModel[] => {
    return this.recipeResource.value() ?? []
  })

  private updateRecipes(recipe: RecipeModel): void {
    this.recipeResource.update((value) => [recipe, ...value!])
  }

  public delete(id: string): void {
    firstValueFrom(this.recipeGateway.delete(API_ENDPOINTS.recipe, id)).then(
      () => this.recipeResource.reload(),
    )
  }

  async save(recipe: RecipeModel) {
    firstValueFrom(this.recipeGateway.post(API_ENDPOINTS.recipe, recipe)).then(
      (result: RecipeModel): void => {
        this.updateRecipes(result)
      },
    )
  }
}
