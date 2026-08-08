import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
} from '@angular/core'
import { Dialog, DialogRef } from '@angular/cdk/dialog'
import { ButtonComponent } from '../../button/button.component'
import { AlertComponent } from '../../alert/alert.component'
import { CsvImportService } from '../../../services/csv-import.service'

@Component({
  selector: 'app-csv-import-progress-dialog',
  imports: [ButtonComponent, AlertComponent],
  templateUrl: './csv-import-progress-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class CsvImportProgressDialogComponent extends Dialog {
  protected readonly csvImportService = inject(CsvImportService)
  private readonly dialogRef = inject(DialogRef)

  constructor() {
    super()
    // Opened with disableClose so an in-progress import can't be dismissed by accident via
    // backdrop click or Escape - once it reaches a terminal phase, closing is safe again.
    effect(() => {
      const phase = this.csvImportService.phase()
      this.dialogRef.disableClose = phase !== 'completed' && phase !== 'failed'
    })
  }

  close(): void {
    this.dialogRef.close()
  }

  retry(): void {
    this.csvImportService.retry()
  }
}
