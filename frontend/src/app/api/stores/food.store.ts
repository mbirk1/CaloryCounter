import {
  computed,
  inject,
  Injectable,
  resource,
  ResourceRef,
  Signal,
  signal,
  WritableSignal,
} from '@angular/core'
import { FoodModel } from '../../models/FoodModel'
import { PageResponse } from '../../models/PageResponse'
import { Gateway } from '../gateways/gateway'
import { firstValueFrom } from 'rxjs'
import { API_ENDPOINTS } from '../../../environment/endpoints'

const DEFAULT_PAGE_SIZE = 20

@Injectable({
  providedIn: 'root',
})
export class FoodStore {
  private readonly foodGateway = inject<Gateway<FoodModel>>(Gateway)

  private readonly pageSignal: WritableSignal<number> = signal(0)

  constructor(private foodGateway: Gateway) {}

  foodResource: ResourceRef<PageResponse<FoodModel> | undefined> = resource({
    request: () => this.pageSignal(),
    loader: async ({ request: page }): Promise<PageResponse<FoodModel>> =>
      firstValueFrom(
        this.foodGateway.get<PageResponse<FoodModel>>(
          `${API_ENDPOINTS.food}?page=${page}&size=${DEFAULT_PAGE_SIZE}`,
        ),
      ),
  })

  readonly foods: Signal<FoodModel[]> = computed(
    () => this.foodResource.value()?.content ?? [],
  )

  readonly currentPage: Signal<number> = computed(
    () => this.foodResource.value()?.page ?? 0,
  )

  readonly totalPages: Signal<number> = computed(
    () => this.foodResource.value()?.totalPages ?? 0,
  )

  readonly totalElements: Signal<number> = computed(
    () => this.foodResource.value()?.totalElements ?? 0,
  )

  readonly isLastPage: Signal<boolean> = computed(
    () => this.foodResource.value()?.last ?? true,
  )

  public delete(id: string): void {
    firstValueFrom(
      this.foodGateway.delete<FoodModel>(API_ENDPOINTS.food, id),
    ).then(() => this.foodResource.reload())
  }

  async save(food: FoodModel) {
    firstValueFrom(
      this.foodGateway.post<FoodModel>(API_ENDPOINTS.food, food),
    ).then(() => this.foodResource.reload())
  }

  load(): boolean {
    return this.foodResource.reload()
  }

  nextPage(): void {
    if (!this.isLastPage()) {
      this.pageSignal.update((page) => page + 1)
    }
  }

  previousPage(): void {
    this.pageSignal.update((page) => Math.max(0, page - 1))
  }

  goToPage(page: number): void {
    this.pageSignal.set(Math.max(0, page))
  }
}
