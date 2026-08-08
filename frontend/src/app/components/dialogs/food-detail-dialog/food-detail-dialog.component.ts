import { ChangeDetectionStrategy, Component, inject } from '@angular/core'
import { Dialog, DIALOG_DATA } from '@angular/cdk/dialog'
import { LabelComponent } from '../../label/label.component'
import { FoodModel } from '../../../models/FoodModel'

@Component({
  selector: 'app-food-detail-dialog',
  imports: [LabelComponent],
  templateUrl: './food-detail-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class FoodDetailDialogComponent extends Dialog {
  data: FoodModel = inject(DIALOG_DATA)
}
