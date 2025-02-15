import { Component, inject } from '@angular/core'
import { Dialog, DIALOG_DATA, DialogRef } from '@angular/cdk/dialog'
import { TextInputComponent } from '../../inputs/text-input/text-input.component'
import { ButtonComponent } from '../../button/button.component'
import { LabelComponent } from '../../label/label.component'

@Component({
  selector: 'app-add-food-dialog',
  imports: [TextInputComponent, ButtonComponent, LabelComponent],
  templateUrl: './add-food-dialog.component.html',
  styles: '',
})
export class AddFoodDialogComponent extends Dialog {
  dialogRef = inject<DialogRef<string>>(DialogRef<string>)
  data = inject(DIALOG_DATA)
  constructor() {
    super()
  }
}
