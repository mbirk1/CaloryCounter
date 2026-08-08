import { TestBed, fakeAsync, tick } from '@angular/core/testing'
import { provideHttpClient } from '@angular/common/http'
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing'
import { FoodStore } from './food.store'
import { ToastService } from '../../services/toast.service'
import { API_ENDPOINTS } from '../../../environment/endpoints'

describe('FoodStore', () => {
  let store: FoodStore
  let httpMock: HttpTestingController
  let toastService: ToastService

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    })
    store = TestBed.inject(FoodStore)
    httpMock = TestBed.inject(HttpTestingController)
    toastService = TestBed.inject(ToastService)
  })

  it('starts with no search, no diet filter, and name ascending', () => {
    expect(store.search()).toBeNull()
    expect(store.diet()).toBeNull()
    expect(store.sort()).toBe('name')
    expect(store.direction()).toBe('asc')
  })

  it('trims the search term and treats a blank value as no filter', () => {
    store.setSearch('  Apfel  ')
    expect(store.search()).toBe('Apfel')

    store.setSearch('   ')
    expect(store.search()).toBeNull()
  })

  it('sets and clears the diet filter', () => {
    store.setDiet('VEGAN')
    expect(store.diet()).toBe('VEGAN')

    store.setDiet(null)
    expect(store.diet()).toBeNull()
  })

  it('starts a newly selected sort field ascending', () => {
    store.setSort('calory')

    expect(store.sort()).toBe('calory')
    expect(store.direction()).toBe('asc')
  })

  it('toggles direction when sorting by the already-active field again', () => {
    store.setSort('name')
    expect(store.sort()).toBe('name')
    expect(store.direction()).toBe('desc')

    store.setSort('name')
    expect(store.direction()).toBe('asc')
  })

  it('resets to ascending when switching to a different field', () => {
    store.setSort('name')
    expect(store.direction()).toBe('desc')

    store.setSort('grams')
    expect(store.sort()).toBe('grams')
    expect(store.direction()).toBe('asc')
  })

  describe('delete() error handling', () => {
    it('shows a success toast when the delete request succeeds', fakeAsync(() => {
      store.delete('food-1')
      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'DELETE',
      })
      req.flush({})
      tick()

      expect(toastService.toasts()[0].variant).toBe('success')
      expect(toastService.toasts()[0].message).toBe(
        'Lebensmittel wurde erfolgreich gelöscht.',
      )
    }))

    it('shows a conflict-specific error toast on 409', fakeAsync(() => {
      store.delete('food-1')
      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'DELETE',
      })
      req.flush('conflict', { status: 409, statusText: 'Conflict' })
      tick()

      expect(toastService.toasts()[0].variant).toBe('error')
      expect(toastService.toasts()[0].message).toBe(
        'Dieses Lebensmittel ist Teil eines Rezepts und kann nicht gelöscht werden.',
      )
    }))

    it('shows an already-deleted error toast on 404', fakeAsync(() => {
      store.delete('food-1')
      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'DELETE',
      })
      req.flush('not found', { status: 404, statusText: 'Not Found' })
      tick()

      expect(toastService.toasts()[0].message).toBe(
        'Dieses Lebensmittel wurde bereits gelöscht.',
      )
    }))

    it('shows a connection error toast on a network failure (status 0)', fakeAsync(() => {
      store.delete('food-1')
      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'DELETE',
      })
      req.error(new ProgressEvent('error'), { status: 0 })
      tick()

      expect(toastService.toasts()[0].message).toBe(
        'Verbindung zum Server fehlgeschlagen. Bitte versuche es erneut.',
      )
    }))

    it('shows a generic error toast for any other status', fakeAsync(() => {
      store.delete('food-1')
      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'DELETE',
      })
      req.flush('boom', { status: 500, statusText: 'Internal Server Error' })
      tick()

      expect(toastService.toasts()[0].message).toBe(
        'Löschen fehlgeschlagen. Bitte versuche es erneut.',
      )
    }))
  })

  describe('isDeleting() / double-submission guard', () => {
    it('marks an id as deleting while the request is in flight, then clears it', fakeAsync(() => {
      expect(store.isDeleting('food-1')).toBe(false)

      store.delete('food-1')
      expect(store.isDeleting('food-1')).toBe(true)

      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'DELETE',
      })
      req.flush({})
      tick()

      expect(store.isDeleting('food-1')).toBe(false)
    }))

    it('clears the pending state even when the delete fails', fakeAsync(() => {
      store.delete('food-1')

      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'DELETE',
      })
      req.flush('conflict', { status: 409, statusText: 'Conflict' })
      tick()

      expect(store.isDeleting('food-1')).toBe(false)
    }))

    it('ignores a second delete call for the same id while the first is still pending', fakeAsync(() => {
      store.delete('food-1')
      store.delete('food-1')

      const requests = httpMock.match({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'DELETE',
      })
      expect(requests).toHaveSize(1)

      requests[0].flush({})
      tick()
    }))

    it('allows deleting a different id while one delete is still pending', fakeAsync(() => {
      store.delete('food-1')
      store.delete('food-2')

      const req1 = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'DELETE',
      })
      const req2 = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-2`,
        method: 'DELETE',
      })
      req1.flush({})
      req2.flush({})
      tick()
    }))
  })

  describe('update()', () => {
    it('resolves true and shows a success toast when the update succeeds', fakeAsync(() => {
      let result: boolean | undefined
      store
        .update('food-1', {
          uuid: 'food-1',
          name: 'Tofu',
          calory: 100,
          grams: 100,
        })
        .then((r) => (result = r))

      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'PUT',
      })
      req.flush({ uuid: 'food-1', name: 'Tofu', calory: 100, grams: 100 })
      tick()

      expect(result).toBe(true)
      expect(toastService.toasts()[0].message).toBe(
        'Lebensmittel wurde aktualisiert.',
      )
    }))

    it('resolves false and shows an error toast when the update fails', fakeAsync(() => {
      let result: boolean | undefined
      store
        .update('food-1', {
          uuid: 'food-1',
          name: 'Tofu',
          calory: 100,
          grams: 100,
        })
        .then((r) => (result = r))

      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.food}/food-1`,
        method: 'PUT',
      })
      req.flush('not acceptable', {
        status: 406,
        statusText: 'Not Acceptable',
      })
      tick()

      expect(result).toBe(false)
      expect(toastService.toasts()[0].variant).toBe('error')
    }))
  })
})
