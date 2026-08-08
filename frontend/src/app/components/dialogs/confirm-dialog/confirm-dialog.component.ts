import { ChangeDetectionStrategy, Component, inject } from '@angular/core'
import { Dialog, DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { ButtonComponent } from '../../button/button.component'

export interface ConfirmDialogData {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
}

// Deliberately generic (not food-specific) - the same "are you sure?" dialog is needed
// wherever a destructive action needs a redundant confirmation, e.g. recipes later on.
@Component({
  selector: 'app-confirm-dialog',
  imports: [ButtonComponent],
  templateUrl: './confirm-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class ConfirmDialogComponent extends Dialog {
  data: ConfirmDialogData = inject(DIALOG_DATA)
  private readonly dialogRef = inject(DialogRef<boolean>)

  protected readonly confirmText = this.data.confirmText ?? 'Bestätigen'
  protected readonly cancelText = this.data.cancelText ?? 'Abbrechen'

  confirm(): void {
    this.dialogRef.close(true)
  }

  cancel(): void {
    this.dialogRef.close(false)
  }
}
