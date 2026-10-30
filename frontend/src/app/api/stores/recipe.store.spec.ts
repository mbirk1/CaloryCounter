import { TestBed, fakeAsync, tick } from '@angular/core/testing'
import { provideHttpClient } from '@angular/common/http'
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing'
import { RecipeStore } from './recipe.store'
import { ToastService } from '../../services/toast.service'
import { API_ENDPOINTS } from '../../../environment/endpoints'
import { CreateRecipePayload } from '../../models/RecipeModel'

describe('RecipeStore', () => {
  let store: RecipeStore
  let httpMock: HttpTestingController
  let toastService: ToastService

  const payload: CreateRecipePayload = {
    name: 'Bowl',
    servings: 2,
    foods: [{ foodId: 'food-1', amountGrams: 150 }],
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    })
    store = TestBed.inject(RecipeStore)
    httpMock = TestBed.inject(HttpTestingController)
    toastService = TestBed.inject(ToastService)
  })

  describe('save()', () => {
    it('resolves true and shows a success toast when the creation succeeds', fakeAsync(() => {
      let result: boolean | undefined
      store.save(payload).then((r) => (result = r))

      const req = httpMock.expectOne({
        url: API_ENDPOINTS.recipe,
        method: 'POST',
      })
      req.flush({ uuid: 'recipe-1', name: 'Bowl' })
      tick()

      expect(result).toBe(true)
      expect(toastService.toasts()[0].variant).toBe('success')
      expect(toastService.toasts()[0].message).toBe(
        'Rezept wurde erfolgreich angelegt.',
      )
    }))

    it('resolves false and shows a validation error toast on 406', fakeAsync(() => {
      let result: boolean | undefined
      store.save(payload).then((r) => (result = r))

      const req = httpMock.expectOne({
        url: API_ENDPOINTS.recipe,
        method: 'POST',
      })
      req.flush('not acceptable', { status: 406, statusText: 'Not Acceptable' })
      tick()

      expect(result).toBe(false)
      expect(toastService.toasts()[0].variant).toBe('error')
      expect(toastService.toasts()[0].message).toBe(
        'Bitte überprüfe deine Eingaben.',
      )
    }))

    it('resolves false and shows a not-found error toast on 404', fakeAsync(() => {
      let result: boolean | undefined
      store.save(payload).then((r) => (result = r))

      const req = httpMock.expectOne({
        url: API_ENDPOINTS.recipe,
        method: 'POST',
      })
      req.flush('not found', { status: 404, statusText: 'Not Found' })
      tick()

      expect(result).toBe(false)
      expect(toastService.toasts()[0].message).toBe(
        'Ein referenziertes Lebensmittel wurde nicht gefunden.',
      )
    }))

    it('resolves false and shows a connection error toast on a network failure', fakeAsync(() => {
      let result: boolean | undefined
      store.save(payload).then((r) => (result = r))

      const req = httpMock.expectOne({
        url: API_ENDPOINTS.recipe,
        method: 'POST',
      })
      req.error(new ProgressEvent('error'), { status: 0 })
      tick()

      expect(result).toBe(false)
      expect(toastService.toasts()[0].message).toBe(
        'Verbindung zum Server fehlgeschlagen. Bitte versuche es erneut.',
      )
    }))

    it('resolves false and shows a generic error toast for any other status', fakeAsync(() => {
      let result: boolean | undefined
      store.save(payload).then((r) => (result = r))

      const req = httpMock.expectOne({
        url: API_ENDPOINTS.recipe,
        method: 'POST',
      })
      req.flush('boom', { status: 500, statusText: 'Internal Server Error' })
      tick()

      expect(result).toBe(false)
      expect(toastService.toasts()[0].message).toBe(
        'Anlegen fehlgeschlagen. Bitte versuche es erneut.',
      )
    }))
  })

  describe('delete()', () => {
    it('shows a success toast when the delete request succeeds', fakeAsync(() => {
      store.delete('recipe-1')
      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.recipe}/recipe-1`,
        method: 'DELETE',
      })
      req.flush({})
      tick()

      expect(toastService.toasts()[0].variant).toBe('success')
      expect(toastService.toasts()[0].message).toBe(
        'Rezept wurde erfolgreich gelöscht.',
      )
    }))

    it('shows an already-deleted error toast on 404', fakeAsync(() => {
      store.delete('recipe-1')
      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.recipe}/recipe-1`,
        method: 'DELETE',
      })
      req.flush('not found', { status: 404, statusText: 'Not Found' })
      tick()

      expect(toastService.toasts()[0].variant).toBe('error')
      expect(toastService.toasts()[0].message).toBe(
        'Dieses Rezept wurde bereits gelöscht.',
      )
    }))
  })
})
