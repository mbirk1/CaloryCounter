import { Injectable, resource, signal, WritableSignal } from '@angular/core'
import { FoodModel } from '../../models/FoodModel'
import { Gateway } from '../gateways/gateway'
import { firstValueFrom } from 'rxjs'
import { API_ENDPOINTS } from '../../../environment/endpoints'

@Injectable({
  providedIn: 'root',
})
export class FoodStore {
  constructor(private foodGateway: Gateway<FoodModel>) {}
  foods: WritableSignal<FoodModel[]> = signal([])

  foodResource = resource({
    request: () => this.foods(),
    loader: async ({ request }) => {
      return firstValueFrom(await this.foodGateway.get(API_ENDPOINTS.food))
    },
  })

  private updateFoods(food: FoodModel): void {
    this.foodResource.update(() => food)
  }

  async save(food: FoodModel) {
    firstValueFrom(await this.foodGateway.post(API_ENDPOINTS.food, food)).then(
      (result: FoodModel): void => {
        this.updateFoods(result)
      },
    )
  }
}
