import { TestBed, fakeAsync, tick } from '@angular/core/testing'
import { provideHttpClient } from '@angular/common/http'
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing'
import { DuplicateStore } from './duplicate.store'
import { ToastService } from '../../services/toast.service'
import { API_ENDPOINTS } from '../../../environment/endpoints'
import { DuplicateAnalysisJobStatus } from '../../models/DuplicateAnalysisJobStatus'

describe('DuplicateStore', () => {
  let store: DuplicateStore
  let httpMock: HttpTestingController
  let toastService: ToastService

  function jobStatus(
    overrides: Partial<DuplicateAnalysisJobStatus>,
  ): DuplicateAnalysisJobStatus {
    return {
      jobId: 'job-1',
      source: 'BLS',
      state: 'RUNNING',
      clustersFound: 0,
      startedAt: '2026-01-01T00:00:00Z',
      ...overrides,
    }
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    })
    store = TestBed.inject(DuplicateStore)
    httpMock = TestBed.inject(HttpTestingController)
    toastService = TestBed.inject(ToastService)
  })

  describe('analyze()', () => {
    it('polls until COMPLETED and reloads the cluster list', fakeAsync(() => {
      store.analyze('BLS')

      const startReq = httpMock.expectOne({
        url: `${API_ENDPOINTS.foodDuplicatesAnalyze}?source=BLS`,
        method: 'POST',
      })
      startReq.flush(jobStatus({ state: 'RUNNING' }))
      tick()
      expect(store.isAnalyzing()).toBeTrue()

      const pollReq = httpMock.expectOne(
        `${API_ENDPOINTS.foodDuplicatesAnalyze}/job-1`,
      )
      pollReq.flush(jobStatus({ state: 'RUNNING' }))
      tick(1000)

      const secondPollReq = httpMock.expectOne(
        `${API_ENDPOINTS.foodDuplicatesAnalyze}/job-1`,
      )
      secondPollReq.flush(jobStatus({ state: 'COMPLETED', clustersFound: 3 }))
      tick()

      expect(store.analysisPhase()).toBe('completed')
      expect(store.analysisJobStatus()?.clustersFound).toBe(3)
    }))

    it('sets the failed phase and error message when the job fails', fakeAsync(() => {
      store.analyze('MANUAL')

      const startReq = httpMock.expectOne({
        url: `${API_ENDPOINTS.foodDuplicatesAnalyze}?source=MANUAL`,
        method: 'POST',
      })
      startReq.flush(jobStatus({ jobId: 'job-2', source: 'MANUAL' }))
      tick()

      const pollReq = httpMock.expectOne(
        `${API_ENDPOINTS.foodDuplicatesAnalyze}/job-2`,
      )
      pollReq.flush(
        jobStatus({
          jobId: 'job-2',
          source: 'MANUAL',
          state: 'FAILED',
          errorMessage: 'boom',
        }),
      )
      tick()

      expect(store.analysisPhase()).toBe('failed')
      expect(store.analysisErrorMessage()).toBe('boom')
    }))

    it('sets a connection-error message when starting the analysis fails outright', fakeAsync(() => {
      store.analyze('OPENFOODFACTS')

      const startReq = httpMock.expectOne({
        url: `${API_ENDPOINTS.foodDuplicatesAnalyze}?source=OPENFOODFACTS`,
        method: 'POST',
      })
      startReq.error(new ProgressEvent('error'), { status: 0 })
      tick()

      expect(store.analysisPhase()).toBe('failed')
      expect(store.analysisErrorMessage()).toBe(
        'Verbindung zum Server fehlgeschlagen. Bitte versuche es erneut.',
      )
    }))

    it('does not start a second analysis while one is already running', fakeAsync(() => {
      store.analyze('BLS')
      const startReq = httpMock.expectOne({
        url: `${API_ENDPOINTS.foodDuplicatesAnalyze}?source=BLS`,
        method: 'POST',
      })

      store.analyze('BLS')
      httpMock.expectNone({
        url: `${API_ENDPOINTS.foodDuplicatesAnalyze}?source=BLS`,
        method: 'POST',
      })

      startReq.flush(jobStatus({ state: 'COMPLETED' }))
      tick()
    }))
  })

  describe('merge()', () => {
    it('resolves true and shows a success toast on success', fakeAsync(() => {
      let result: boolean | undefined
      store.merge('cluster-1', 'food-1').then((r) => (result = r))

      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.foodDuplicates}/cluster-1/merge`,
        method: 'POST',
      })
      expect(req.request.body).toEqual({ canonicalFoodId: 'food-1' })
      req.flush({ id: 'cluster-1', status: 'CONFIRMED', members: [] })
      tick()

      expect(result).toBeTrue()
      expect(toastService.toasts()[0].variant).toBe('success')
      expect(toastService.toasts()[0].message).toBe(
        'Cluster wurde erfolgreich zusammengeführt.',
      )
    }))

    it('resolves false and shows an error toast when the cluster is already resolved (406)', fakeAsync(() => {
      let result: boolean | undefined
      store.merge('cluster-1', 'food-1').then((r) => (result = r))

      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.foodDuplicates}/cluster-1/merge`,
        method: 'POST',
      })
      req.flush('not acceptable', { status: 406, statusText: 'Not Acceptable' })
      tick()

      expect(result).toBeFalse()
      expect(toastService.toasts()[0].variant).toBe('error')
    }))

    it('resolves false and shows a not-found toast on 404', fakeAsync(() => {
      let result: boolean | undefined
      store.merge('cluster-1', 'food-1').then((r) => (result = r))

      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.foodDuplicates}/cluster-1/merge`,
        method: 'POST',
      })
      req.flush('not found', { status: 404, statusText: 'Not Found' })
      tick()

      expect(result).toBeFalse()
      expect(toastService.toasts()[0].message).toBe(
        'Dieser Cluster wurde bereits bearbeitet oder existiert nicht mehr.',
      )
    }))
  })

  describe('reject()', () => {
    it('resolves true and shows a success toast on success', fakeAsync(() => {
      let result: boolean | undefined
      store.reject('cluster-2').then((r) => (result = r))

      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.foodDuplicates}/cluster-2/reject`,
        method: 'POST',
      })
      req.flush({ id: 'cluster-2', status: 'REJECTED', members: [] })
      tick()

      expect(result).toBeTrue()
      expect(toastService.toasts()[0].message).toBe(
        'Cluster wurde als kein Duplikat markiert.',
      )
    }))

    it('resolves false and shows a generic error toast for an unexpected status', fakeAsync(() => {
      let result: boolean | undefined
      store.reject('cluster-2').then((r) => (result = r))

      const req = httpMock.expectOne({
        url: `${API_ENDPOINTS.foodDuplicates}/cluster-2/reject`,
        method: 'POST',
      })
      req.flush('boom', { status: 500, statusText: 'Internal Server Error' })
      tick()

      expect(result).toBeFalse()
      expect(toastService.toasts()[0].message).toBe(
        'Ablehnen fehlgeschlagen. Bitte versuche es erneut.',
      )
    }))
  })
})
