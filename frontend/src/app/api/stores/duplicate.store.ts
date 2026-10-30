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
import { firstValueFrom } from 'rxjs'
import { DuplicateClusterModel } from '../../models/DuplicateClusterModel'
import { DuplicateAnalysisJobStatus } from '../../models/DuplicateAnalysisJobStatus'
import { PageResponse } from '../../models/PageResponse'
import { Gateway } from '../gateways/gateway'
import { API_ENDPOINTS } from '../../../environment/endpoints'
import { ToastService } from '../../services/toast.service'

export type AnalysisPhase = 'idle' | 'running' | 'completed' | 'failed'

const POLL_INTERVAL_MS = 1000
const CLUSTER_PAGE_SIZE = 100

// Mirrors CsvImportService's polling pattern (upload -> job id -> poll until a terminal state),
// but without the upload/progress-event part - an analysis run has no file to upload, it starts
// straight from a POST with just the source as a query param.
@Injectable({
  providedIn: 'root',
})
export class DuplicateStore {
  private readonly duplicateGateway =
    inject<Gateway<DuplicateClusterModel>>(Gateway)
  private readonly toastService = inject(ToastService)

  private readonly phaseSignal: WritableSignal<AnalysisPhase> = signal('idle')
  private readonly jobStatusSignal: WritableSignal<DuplicateAnalysisJobStatus | null> =
    signal(null)
  private readonly errorMessageSignal: WritableSignal<string | null> =
    signal(null)

  readonly analysisPhase: Signal<AnalysisPhase> = this.phaseSignal.asReadonly()
  readonly analysisJobStatus: Signal<DuplicateAnalysisJobStatus | null> =
    this.jobStatusSignal.asReadonly()
  readonly analysisErrorMessage: Signal<string | null> =
    this.errorMessageSignal.asReadonly()
  readonly isAnalyzing: Signal<boolean> = computed(
    () => this.phaseSignal() === 'running',
  )

  clusterResource: ResourceRef<DuplicateClusterModel[] | undefined> = resource({
    loader: async (): Promise<DuplicateClusterModel[]> => {
      const response = await firstValueFrom(
        this.duplicateGateway.get<PageResponse<DuplicateClusterModel>>(
          `${API_ENDPOINTS.foodDuplicates}?status=PENDING&size=${CLUSTER_PAGE_SIZE}`,
        ),
      )
      return response.content
    },
  })

  readonly clusters: Signal<DuplicateClusterModel[]> = computed(
    () => this.clusterResource.value() ?? [],
  )

  load(): boolean {
    return this.clusterResource.reload()
  }

  async analyze(source: string): Promise<void> {
    if (this.isAnalyzing()) {
      return
    }
    this.phaseSignal.set('running')
    this.errorMessageSignal.set(null)
    this.jobStatusSignal.set(null)
    try {
      const job = await firstValueFrom(
        this.duplicateGateway.post<DuplicateAnalysisJobStatus>(
          `${API_ENDPOINTS.foodDuplicatesAnalyze}?source=${source}`,
          {},
        ),
      )
      this.jobStatusSignal.set(job)
      await this.pollUntilFinished(job.jobId)
    } catch (error) {
      this.phaseSignal.set('failed')
      this.errorMessageSignal.set(this.toAnalysisErrorMessage(error))
    }
  }

  async merge(clusterId: string, canonicalFoodId: string): Promise<boolean> {
    try {
      await firstValueFrom(
        this.duplicateGateway.post(
          `${API_ENDPOINTS.foodDuplicates}/${clusterId}/merge`,
          { canonicalFoodId },
        ),
      )
      this.toastService.success('Cluster wurde erfolgreich zusammengeführt.')
      this.clusterResource.reload()
      return true
    } catch (error) {
      this.toastService.error(
        this.mapErrorToMessage(
          error,
          'Zusammenführen fehlgeschlagen. Bitte versuche es erneut.',
        ),
      )
      return false
    }
  }

  async reject(clusterId: string): Promise<boolean> {
    try {
      await firstValueFrom(
        this.duplicateGateway.post(
          `${API_ENDPOINTS.foodDuplicates}/${clusterId}/reject`,
          {},
        ),
      )
      this.toastService.success('Cluster wurde als kein Duplikat markiert.')
      this.clusterResource.reload()
      return true
    } catch (error) {
      this.toastService.error(
        this.mapErrorToMessage(
          error,
          'Ablehnen fehlgeschlagen. Bitte versuche es erneut.',
        ),
      )
      return false
    }
  }

  private async pollUntilFinished(jobId: string): Promise<void> {
    const status = await firstValueFrom(
      this.duplicateGateway.get<DuplicateAnalysisJobStatus>(
        `${API_ENDPOINTS.foodDuplicatesAnalyze}/${jobId}`,
      ),
    )
    this.jobStatusSignal.set(status)

    if (status.state === 'RUNNING') {
      await new Promise((resolvePromise) =>
        setTimeout(resolvePromise, POLL_INTERVAL_MS),
      )
      await this.pollUntilFinished(jobId)
      return
    }

    if (status.state === 'COMPLETED') {
      this.phaseSignal.set('completed')
      this.clusterResource.reload()
    } else {
      this.phaseSignal.set('failed')
      this.errorMessageSignal.set(
        status.errorMessage ??
          'Die Analyse ist fehlgeschlagen. Bitte versuche es erneut.',
      )
    }
  }

  private mapErrorToMessage(error: unknown, genericMessage: string): string {
    if (!(error instanceof HttpErrorResponse)) {
      return genericMessage
    }
    switch (error.status) {
      case 404:
        return 'Dieser Cluster wurde bereits bearbeitet oder existiert nicht mehr.'
      case 406:
        return 'Dieser Cluster wurde bereits bearbeitet, oder das gewählte Lebensmittel gehört nicht zum Cluster.'
      case 0:
        return 'Verbindung zum Server fehlgeschlagen. Bitte versuche es erneut.'
      default:
        return genericMessage
    }
  }

  private toAnalysisErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.status === 0
        ? 'Verbindung zum Server fehlgeschlagen. Bitte versuche es erneut.'
        : `Analyse fehlgeschlagen (${error.status}). Bitte versuche es erneut.`
    }
    return 'Es ist ein unerwarteter Fehler aufgetreten.'
  }
}
