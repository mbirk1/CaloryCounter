import {
  computed,
  inject,
  Injectable,
  Signal,
  signal,
  WritableSignal,
} from '@angular/core'
import {
  HttpClient,
  HttpErrorResponse,
  HttpEventType,
} from '@angular/common/http'
import { firstValueFrom } from 'rxjs'
import { API_ENDPOINTS } from '../../environment/endpoints'
import { ImportJobStatus } from '../models/ImportJobStatus'
import { FoodStore } from '../api/stores/food.store'

export type CsvImportPhase =
  'idle' | 'uploading' | 'processing' | 'completed' | 'failed'

const POLL_INTERVAL_MS = 1000

// Lives outside the progress dialog on purpose: the upload and the background processing it
// kicks off must keep running (and keep updating this state) even if the user closes the dialog
// or navigates away, since both are long-running for a multi-gigabyte file. The dialog is just a
// view onto whatever this singleton is currently doing - reopening it (or a fresh page load
// while a job is still RUNNING) would show the current state either way.
@Injectable({ providedIn: 'root' })
export class CsvImportService {
  private readonly http = inject(HttpClient)
  private readonly foodStore = inject(FoodStore)

  private readonly phaseSignal: WritableSignal<CsvImportPhase> = signal('idle')
  private readonly uploadProgressSignal: WritableSignal<number> = signal(0)
  private readonly jobStatusSignal: WritableSignal<ImportJobStatus | null> =
    signal(null)
  private readonly errorMessageSignal: WritableSignal<string | null> =
    signal(null)
  private readonly fileNameSignal: WritableSignal<string | null> = signal(null)

  private lastFile: File | null = null

  readonly phase: Signal<CsvImportPhase> = this.phaseSignal.asReadonly()
  readonly uploadProgress: Signal<number> =
    this.uploadProgressSignal.asReadonly()
  readonly jobStatus: Signal<ImportJobStatus | null> =
    this.jobStatusSignal.asReadonly()
  readonly errorMessage: Signal<string | null> =
    this.errorMessageSignal.asReadonly()
  readonly fileName: Signal<string | null> = this.fileNameSignal.asReadonly()

  readonly isActive: Signal<boolean> = computed(
    () => this.phase() === 'uploading' || this.phase() === 'processing',
  )

  readonly processingProgress: Signal<number> = computed(() => {
    const status = this.jobStatus()
    if (!status || status.totalBytes <= 0) {
      return 0
    }
    return Math.min(
      100,
      Math.round((status.bytesRead / status.totalBytes) * 100),
    )
  })

  upload(file: File): void {
    if (this.isActive()) {
      return
    }
    this.lastFile = file
    this.fileNameSignal.set(file.name)
    this.errorMessageSignal.set(null)
    this.jobStatusSignal.set(null)
    this.uploadProgressSignal.set(0)
    this.phaseSignal.set('uploading')
    this.runUpload(file)
  }

  retry(): void {
    if (this.lastFile) {
      this.upload(this.lastFile)
    }
  }

  private async runUpload(file: File): Promise<void> {
    const formData = new FormData()
    formData.append('file', file)

    try {
      const job = await this.uploadWithProgress(formData)
      this.jobStatusSignal.set(job)
      this.phaseSignal.set('processing')
      await this.pollUntilFinished(job.jobId)
    } catch (error) {
      this.phaseSignal.set('failed')
      this.errorMessageSignal.set(this.toErrorMessage(error))
    }
  }

  private uploadWithProgress(formData: FormData): Promise<ImportJobStatus> {
    return new Promise<ImportJobStatus>((resolve, reject) => {
      this.http
        .request<ImportJobStatus>('POST', API_ENDPOINTS.foodImport, {
          body: formData,
          reportProgress: true,
          observe: 'events',
          withCredentials: true,
        })
        .subscribe({
          next: (event) => {
            if (event.type === HttpEventType.UploadProgress && event.total) {
              this.uploadProgressSignal.set(
                Math.round((event.loaded / event.total) * 100),
              )
            } else if (event.type === HttpEventType.Response) {
              this.uploadProgressSignal.set(100)
              if (event.body) {
                resolve(event.body)
              } else {
                reject(new Error('Leere Antwort vom Server.'))
              }
            }
          },
          error: (error) => reject(error),
        })
    })
  }

  private async pollUntilFinished(jobId: string): Promise<void> {
    const status = await firstValueFrom(
      this.http.get<ImportJobStatus>(`${API_ENDPOINTS.foodImport}/${jobId}`, {
        withCredentials: true,
      }),
    )
    this.jobStatusSignal.set(status)

    if (status.state === 'RUNNING') {
      await new Promise((resolve) => setTimeout(resolve, POLL_INTERVAL_MS))
      await this.pollUntilFinished(jobId)
      return
    }

    if (status.state === 'COMPLETED') {
      this.phaseSignal.set('completed')
      this.foodStore.load()
    } else {
      this.phaseSignal.set('failed')
      this.errorMessageSignal.set(
        'Der Import ist fehlgeschlagen. Bitte versuche es erneut.',
      )
    }
  }

  private toErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.status === 0
        ? 'Verbindung zum Server fehlgeschlagen. Bitte versuche es erneut.'
        : `Hochladen fehlgeschlagen (${error.status}). Bitte versuche es erneut.`
    }
    return 'Es ist ein unerwarteter Fehler aufgetreten.'
  }
}
