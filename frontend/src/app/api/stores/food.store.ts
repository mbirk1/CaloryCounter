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

export type SortField = 'name' | 'calory' | 'grams'
export type SortDirection = 'asc' | 'desc'

interface FoodQuery {
  page: number
  search: string | null
  diet: string | null
  sort: SortField
  direction: SortDirection
}

@Injectable({
  providedIn: 'root',
})
export class FoodStore {
  private readonly foodGateway = inject<Gateway<FoodModel>>(Gateway)

  private readonly pageSignal: WritableSignal<number> = signal(0)
  private readonly searchSignal: WritableSignal<string | null> = signal(null)
  private readonly dietSignal: WritableSignal<string | null> = signal(null)
  private readonly sortSignal: WritableSignal<SortField> = signal('name')
  private readonly directionSignal: WritableSignal<SortDirection> =
    signal('asc')

  foodResource: ResourceRef<PageResponse<FoodModel> | undefined> = resource({
    params: (): FoodQuery => ({
      page: this.pageSignal(),
      search: this.searchSignal(),
      diet: this.dietSignal(),
      sort: this.sortSignal(),
      direction: this.directionSignal(),
    }),
    loader: async ({ params }): Promise<PageResponse<FoodModel>> =>
      firstValueFrom(
        this.foodGateway.get<PageResponse<FoodModel>>(this.buildUrl(params)),
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

  readonly search: Signal<string | null> = this.searchSignal.asReadonly()
  readonly diet: Signal<string | null> = this.dietSignal.asReadonly()
  readonly sort: Signal<SortField> = this.sortSignal.asReadonly()
  readonly direction: Signal<SortDirection> = this.directionSignal.asReadonly()

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

  // Jumps back to page 1 - staying on a page from before could land the user on a now-empty
  // or out-of-range page for the new, narrower result set.
  setSearch(search: string): void {
    this.searchSignal.set(search.trim().length === 0 ? null : search.trim())
    this.pageSignal.set(0)
  }

  // Sets the diet filter (or clears it with null) and jumps back to page 1, same reason as
  // setSearch above.
  setDiet(diet: string | null): void {
    this.dietSignal.set(diet)
    this.pageSignal.set(0)
  }

  // A click on the already-active field toggles direction; a click on a different field starts
  // it ascending. The current page is deliberately kept - only search/diet changes reset it,
  // since re-sorting doesn't change how many results exist.
  setSort(field: SortField): void {
    if (this.sortSignal() === field) {
      this.directionSignal.update((direction) =>
        direction === 'asc' ? 'desc' : 'asc',
      )
    } else {
      this.sortSignal.set(field)
      this.directionSignal.set('asc')
    }
  }

  private buildUrl(query: FoodQuery): string {
    const params = new URLSearchParams()
    params.set('page', String(query.page))
    params.set('size', String(DEFAULT_PAGE_SIZE))
    params.set('sort', query.sort)
    params.set('direction', query.direction)
    if (query.search) {
      params.set('search', query.search)
    }
    if (query.diet) {
      params.set('diet', query.diet)
    }
    return `${API_ENDPOINTS.food}?${params.toString()}`
  }
}
