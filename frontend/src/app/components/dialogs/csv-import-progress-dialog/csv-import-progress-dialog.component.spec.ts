import { TestBed } from '@angular/core/testing'
import { signal } from '@angular/core'
import { DialogRef } from '@angular/cdk/dialog'
import { CsvImportProgressDialogComponent } from './csv-import-progress-dialog.component'
import { CsvImportService } from '../../../services/csv-import.service'
import { ImportJobStatus } from '../../../models/ImportJobStatus'

describe('CsvImportProgressDialogComponent', () => {
  let dialogRefSpy: jasmine.SpyObj<DialogRef>
  let csvImportServiceStub: {
    phase: ReturnType<typeof signal<string>>
    uploadProgress: ReturnType<typeof signal<number>>
    processingProgress: ReturnType<typeof signal<number>>
    jobStatus: ReturnType<typeof signal<ImportJobStatus | null>>
    errorMessage: ReturnType<typeof signal<string | null>>
    fileName: ReturnType<typeof signal<string | null>>
    retry: jasmine.Spy
  }

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
    dialogRefSpy = jasmine.createSpyObj<DialogRef>('DialogRef', ['close'])
    csvImportServiceStub = {
      phase: signal('idle'),
      uploadProgress: signal(0),
      processingProgress: signal(0),
      jobStatus: signal(null),
      errorMessage: signal(null),
      fileName: signal(null),
      retry: jasmine.createSpy('retry'),
    }

    TestBed.configureTestingModule({
      imports: [CsvImportProgressDialogComponent],
      providers: [
        { provide: DialogRef, useValue: dialogRefSpy },
        { provide: CsvImportService, useValue: csvImportServiceStub },
      ],
    })
  })

  function create() {
    const fixture = TestBed.createComponent(CsvImportProgressDialogComponent)
    fixture.detectChanges()
    return fixture
  }

  it('shows the upload progress bar and percentage while uploading', () => {
    csvImportServiceStub.phase.set('uploading')
    csvImportServiceStub.uploadProgress.set(42)
    const fixture = create()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Hochladen')
    expect(text).toContain('42%')
  })

  it('shows the processing progress with row counts', () => {
    csvImportServiceStub.phase.set('processing')
    csvImportServiceStub.processingProgress.set(17)
    csvImportServiceStub.jobStatus.set(
      jobStatus({ processedRows: 500, importedCount: 480, skippedCount: 20 }),
    )
    const fixture = create()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Verarbeiten')
    expect(text).toContain('17%')
    expect(text).toContain('500')
    expect(text).toContain('480')
  })

  it('shows a success summary and closes the dialog on click when completed', () => {
    csvImportServiceStub.phase.set('completed')
    csvImportServiceStub.jobStatus.set(
      jobStatus({
        state: 'COMPLETED',
        importedCount: 10,
        skippedCount: 2,
        errorCount: 0,
      }),
    )
    const fixture = create()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('abgeschlossen')

    const closeButton = (
      fixture.nativeElement as HTMLElement
    ).querySelector<HTMLButtonElement>('app-button button')
    closeButton?.click()

    expect(dialogRefSpy.close).toHaveBeenCalled()
  })

  it('shows the error message and calls retry when failed', () => {
    csvImportServiceStub.phase.set('failed')
    csvImportServiceStub.errorMessage.set('Etwas ist schiefgelaufen.')
    const fixture = create()

    const text = (fixture.nativeElement as HTMLElement).textContent ?? ''
    expect(text).toContain('Etwas ist schiefgelaufen.')

    const buttons = (
      fixture.nativeElement as HTMLElement
    ).querySelectorAll<HTMLButtonElement>('app-button button')
    const retryButton = Array.from(buttons).find((button) =>
      (button.textContent ?? '').includes('Erneut versuchen'),
    )
    retryButton?.click()

    expect(csvImportServiceStub.retry).toHaveBeenCalled()
  })
})
