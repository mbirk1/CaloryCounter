import {
  computed,
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
  constructor(private recipeGateway: Gateway) {}

  recipeResource: ResourceRef<RecipeModel[] | undefined> = resource({
    loader: async (): Promise<RecipeModel[]> =>
      firstValueFrom(
        this.recipeGateway.get<RecipeModel[]>(API_ENDPOINTS.recipe),
      ),
  })

  readonly recipes: Signal<RecipeModel[]> = computed((): RecipeModel[] => {
    return this.recipeResource.value() ?? []
  })

  private updateRecipes(recipe: RecipeModel): void {
    this.recipeResource.update((value) => [recipe, ...value!])
  }

  public delete(id: string): void {
    firstValueFrom(
      this.recipeGateway.delete<RecipeModel>(API_ENDPOINTS.recipe, id),
    ).then(() => this.recipeResource.reload())
  }

  async save(recipe: RecipeModel) {
    firstValueFrom(
      this.recipeGateway.post<RecipeModel>(API_ENDPOINTS.recipe, recipe),
    ).then((result: RecipeModel): void => {
      this.updateRecipes(result)
    })
  }
}
