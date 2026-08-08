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
import { HttpErrorResponse } from '@angular/common/http'
import { FoodModel } from '../../models/FoodModel'
import { PageResponse } from '../../models/PageResponse'
import { Gateway } from '../gateways/gateway'
import { firstValueFrom } from 'rxjs'
import { API_ENDPOINTS } from '../../../environment/endpoints'
import { ToastService } from '../../services/toast.service'

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
  private readonly toastService = inject(ToastService)

  private readonly pageSignal: WritableSignal<number> = signal(0)
  private readonly searchSignal: WritableSignal<string | null> = signal(null)
  private readonly dietSignal: WritableSignal<string | null> = signal(null)
  private readonly sortSignal: WritableSignal<SortField> = signal('name')
  private readonly directionSignal: WritableSignal<SortDirection> =
    signal('asc')

  // Tracks ids with a delete request currently in flight. The table reload after a successful
  // delete isn't instant (it's a fresh network round trip), so without this a row stays
  // clickable in the gap between confirming the delete and the table actually losing that row -
  // a user re-clicking "Löschen" on what looks like an unresponsive button would otherwise fire
  // a second delete for an already-gone item and get a confusing "already deleted" error toast.
  private readonly pendingDeletesSignal: WritableSignal<ReadonlySet<string>> =
    signal(new Set())

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

  isDeleting(id: string): boolean {
    return this.pendingDeletesSignal().has(id)
  }

  public delete(id: string): void {
    if (this.isDeleting(id)) {
      return
    }
    this.addPendingDelete(id)
    firstValueFrom(this.foodGateway.delete<FoodModel>(API_ENDPOINTS.food, id))
      .then(() => {
        this.toastService.success('Lebensmittel wurde erfolgreich gelöscht.')
        this.foodResource.reload()
      })
      .catch((error: unknown) => {
        this.toastService.error(
          this.mapErrorToMessage(
            error,
            'Löschen fehlgeschlagen. Bitte versuche es erneut.',
          ),
        )
      })
      .finally(() => this.removePendingDelete(id))
  }

  async save(food: FoodModel) {
    firstValueFrom(
      this.foodGateway.post<FoodModel>(API_ENDPOINTS.food, food),
    ).then(() => this.foodResource.reload())
  }

  // Returns whether the update succeeded, so the edit dialog knows whether to close itself
  // (success) or stay open with the entered values intact (failure).
  async update(id: string, food: FoodModel): Promise<boolean> {
    try {
      await firstValueFrom(
        this.foodGateway.put<FoodModel>(`${API_ENDPOINTS.food}/${id}`, food),
      )
      this.toastService.success('Lebensmittel wurde aktualisiert.')
      this.foodResource.reload()
      return true
    } catch (error) {
      this.toastService.error(
        this.mapErrorToMessage(
          error,
          'Aktualisieren fehlgeschlagen. Bitte versuche es erneut.',
        ),
      )
      return false
    }
  }

  // Maps a failed request's HTTP status to a clear, German, end-user-facing message rather
  // than relying on parsing the raw response body - the backend's error bodies are plain text
  // meant for developers reading logs, not guaranteed stable copy to show end users.
  private mapErrorToMessage(error: unknown, genericMessage: string): string {
    if (!(error instanceof HttpErrorResponse)) {
      return genericMessage
    }
    switch (error.status) {
      case 409:
        return 'Dieses Lebensmittel ist Teil eines Rezepts und kann nicht gelöscht werden.'
      case 404:
        return 'Dieses Lebensmittel wurde bereits gelöscht.'
      case 406:
        return 'Bitte überprüfe deine Eingaben - ein Wert liegt außerhalb des gültigen Bereichs.'
      case 0:
        return 'Verbindung zum Server fehlgeschlagen. Bitte versuche es erneut.'
      default:
        return genericMessage
    }
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

  private addPendingDelete(id: string): void {
    this.pendingDeletesSignal.update((ids) => new Set(ids).add(id))
  }

  private removePendingDelete(id: string): void {
    this.pendingDeletesSignal.update((ids) => {
      const next = new Set(ids)
      next.delete(id)
      return next
    })
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
