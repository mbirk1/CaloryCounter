import { fakeAsync, TestBed, tick } from '@angular/core/testing'
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing'
import { HttpEventType, provideHttpClient } from '@angular/common/http'
import { CsvImportService } from './csv-import.service'
import { FoodStore } from '../api/stores/food.store'
import { API_ENDPOINTS } from '../../environment/endpoints'
import { ImportJobStatus } from '../models/ImportJobStatus'

describe('CsvImportService', () => {
  let service: CsvImportService
  let httpMock: HttpTestingController
  let foodStoreSpy: jasmine.SpyObj<FoodStore>

  const jobStatus = (
    overrides: Partial<ImportJobStatus> = {},
  ): ImportJobStatus => ({
    jobId: 'job-1',
    state: 'RUNNING',
    totalBytes: 1000,
    bytesRead: 0,
    processedRows: 0,
    importedCount: 0,
    skippedCount: 0,
    errorCount: 0,
    startedAt: '2026-07-24T00:00:00Z',
    ...overrides,
  })

  beforeEach(() => {
    foodStoreSpy = jasmine.createSpyObj<FoodStore>('FoodStore', ['load'])

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: FoodStore, useValue: foodStoreSpy },
      ],
    })

    service = TestBed.inject(CsvImportService)
    httpMock = TestBed.inject(HttpTestingController)
  })

  afterEach(() => {
    httpMock.verify()
  })

  function makeFile(): File {
    return new File(['code\tproduct_name\n'], 'test.csv', {
      type: 'text/csv',
    })
  }

  it('starts in the idle phase', () => {
    expect(service.phase()).toBe('idle')
    expect(service.isActive()).toBeFalse()
  })

  it('tracks upload progress and switches to processing once the job is created', fakeAsync(() => {
    service.upload(makeFile())
    expect(service.phase()).toBe('uploading')

    const uploadReq = httpMock.expectOne(API_ENDPOINTS.foodImport)
    uploadReq.event({
      type: HttpEventType.UploadProgress,
      loaded: 50,
      total: 100,
    })
    expect(service.uploadProgress()).toBe(50)

    uploadReq.flush(jobStatus({ state: 'RUNNING' }))
    tick()

    expect(service.phase()).toBe('processing')
    expect(service.uploadProgress()).toBe(100)

    httpMock
      .expectOne(`${API_ENDPOINTS.foodImport}/job-1`)
      .flush(jobStatus({ state: 'COMPLETED' }))
    tick()
  }))

  it('computes processing progress from bytesRead / totalBytes', fakeAsync(() => {
    service.upload(makeFile())
    httpMock
      .expectOne(API_ENDPOINTS.foodImport)
      .flush(jobStatus({ totalBytes: 1000, bytesRead: 250 }))
    tick()

    httpMock
      .expectOne(`${API_ENDPOINTS.foodImport}/job-1`)
      .flush(
        jobStatus({ totalBytes: 1000, bytesRead: 250, state: 'COMPLETED' }),
      )
    tick()

    expect(service.processingProgress()).toBe(25)
  }))

  it('moves to completed and reloads the food store once the job finishes', fakeAsync(() => {
    service.upload(makeFile())
    httpMock.expectOne(API_ENDPOINTS.foodImport).flush(jobStatus())
    tick()

    httpMock
      .expectOne(`${API_ENDPOINTS.foodImport}/job-1`)
      .flush(jobStatus({ state: 'COMPLETED', bytesRead: 1000 }))
    tick()

    expect(service.phase()).toBe('completed')
    expect(service.isActive()).toBeFalse()
    expect(foodStoreSpy.load).toHaveBeenCalled()
  }))

  it('moves to failed when the job status comes back as FAILED', fakeAsync(() => {
    service.upload(makeFile())
    httpMock.expectOne(API_ENDPOINTS.foodImport).flush(jobStatus())
    tick()

    httpMock
      .expectOne(`${API_ENDPOINTS.foodImport}/job-1`)
      .flush(jobStatus({ state: 'FAILED' }))
    tick()

    expect(service.phase()).toBe('failed')
    expect(service.errorMessage()).toContain('fehlgeschlagen')
  }))

  it('moves to failed with a connection error message when the upload request errors', fakeAsync(() => {
    service.upload(makeFile())

    httpMock
      .expectOne(API_ENDPOINTS.foodImport)
      .error(new ProgressEvent('error'), { status: 0 })
    tick()

    expect(service.phase()).toBe('failed')
    expect(service.errorMessage()).toContain('Verbindung')
  }))

  it('ignores a second upload call while one is already active', fakeAsync(() => {
    service.upload(makeFile())
    const firstFileName = service.fileName()

    const otherFile = new File(['a'], 'other.csv', { type: 'text/csv' })
    service.upload(otherFile)

    expect(service.fileName()).toBe(firstFileName)
    httpMock.expectOne(API_ENDPOINTS.foodImport).flush(jobStatus())
    tick()

    httpMock
      .expectOne(`${API_ENDPOINTS.foodImport}/job-1`)
      .flush(jobStatus({ state: 'COMPLETED' }))
    tick()
  }))

  it('retries the last file after a failure', fakeAsync(() => {
    const file = makeFile()
    service.upload(file)
    httpMock
      .expectOne(API_ENDPOINTS.foodImport)
      .error(new ProgressEvent('error'), { status: 0 })
    tick()
    expect(service.phase()).toBe('failed')

    service.retry()
    expect(service.phase()).toBe('uploading')

    httpMock.expectOne(API_ENDPOINTS.foodImport).flush(jobStatus())
    tick()

    httpMock
      .expectOne(`${API_ENDPOINTS.foodImport}/job-1`)
      .flush(jobStatus({ state: 'COMPLETED' }))
    tick()
  }))
})
