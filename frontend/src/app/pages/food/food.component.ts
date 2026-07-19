import {
  Component,
  inject,
  signal,
  WritableSignal,
  ChangeDetectionStrategy,
} from '@angular/core'
import { ButtonComponent } from '../../components/button/button.component'
import { FoodTableComponent } from '../../components/food-table/food-table.component'
import { Dialog } from '@angular/cdk/dialog'
import { AddFoodDialogComponent } from '../../components/dialogs/add-food-dialog/add-food-dialog.component'
import { Gateway } from '../../api/gateways/gateway'
import { FoodStore } from '../../api/stores/food.store'
import { API_ENDPOINTS } from '../../../environment/endpoints'
import { ImportJobStatus } from '../../models/ImportJobStatus'
import { firstValueFrom } from 'rxjs'

const POLL_INTERVAL_MS = 1000

@Component({
  selector: 'app-food',
  imports: [ButtonComponent, FoodTableComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './food.component.html',
})
export class FoodComponent {
  private readonly dialog = inject(Dialog)
  private readonly foodGateway = inject<Gateway<unknown>>(Gateway)
  private readonly foodStore = inject(FoodStore)

  importJob: WritableSignal<ImportJobStatus | null> = signal(null)
  importing: WritableSignal<boolean> = signal(false)

  handleFoodDialog() {
    this.dialog.open(AddFoodDialogComponent, {})
  }

  async onCsvFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file) {
      return
    }

    const formData = new FormData()
    formData.append('file', file)

    this.importing.set(true)
    const job = await firstValueFrom(
      this.foodGateway.post<ImportJobStatus>(
        API_ENDPOINTS.foodImport,
        formData,
      ),
    )
    this.importJob.set(job)
    await this.pollUntilFinished(job.jobId)
  }

  private async pollUntilFinished(jobId: string): Promise<void> {
    const status = await firstValueFrom(
      this.foodGateway.get<ImportJobStatus>(
        `${API_ENDPOINTS.foodImport}/${jobId}`,
      ),
    )
    this.importJob.set(status)

    if (status.state === 'RUNNING') {
      setTimeout(() => this.pollUntilFinished(jobId), POLL_INTERVAL_MS)
      return
    }

    this.importing.set(false)
    if (status.state === 'COMPLETED') {
      this.foodStore.load()
    }
  }
}
