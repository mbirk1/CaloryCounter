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

@Injectable({
  providedIn: 'root',
})
export class FoodStore {
  constructor(private foodGateway: Gateway<FoodModel>) {}

  foodResource: ResourceRef<FoodModel[] | undefined> = resource({
    loader: async (): Promise<FoodModel[]> =>
      firstValueFrom(this.foodGateway.get(API_ENDPOINTS.food)),
  })

  readonly foods: Signal<FoodModel[]> = computed((): FoodModel[] => {
    return this.foodResource.value() ?? []
  })

  private updateFoods(food: FoodModel): void {
    this.foodResource.update((value) => [food, ...value!])
  }

  public delete(id: string): void {
    firstValueFrom(this.foodGateway.delete(API_ENDPOINTS.food, id)).then(() =>
      this.foodResource.reload(),
    )
  }

  async save(food: FoodModel) {
    firstValueFrom(this.foodGateway.post(API_ENDPOINTS.food, food)).then(
      (result: FoodModel): void => {
        this.updateFoods(result)
      },
    )
  }
}
