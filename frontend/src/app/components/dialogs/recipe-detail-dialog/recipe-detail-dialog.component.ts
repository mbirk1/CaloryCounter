import { ChangeDetectionStrategy, Component, inject } from '@angular/core'
import { Dialog, DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome'
import { faXmark } from '@fortawesome/free-solid-svg-icons'
import { CardComponent } from '../../card/card.component'
import { DietBadgeComponent } from '../../diet-badge/diet-badge.component'
import { RecipeNutrientSummaryComponent } from '../../recipe-nutrient-summary/recipe-nutrient-summary.component'
import { RecipeModel } from '../../../models/RecipeModel'

@Component({
  selector: 'app-recipe-detail-dialog',
  imports: [
    CardComponent,
    DietBadgeComponent,
    FontAwesomeModule,
    RecipeNutrientSummaryComponent,
  ],
  templateUrl: './recipe-detail-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class RecipeDetailDialogComponent extends Dialog {
  data: RecipeModel = inject(DIALOG_DATA)
  private readonly dialogRef = inject(DialogRef)

  protected readonly faXmark = faXmark

  protected get totalAmountGrams(): number {
    return this.data.foods.reduce((sum, entry) => sum + entry.amountGrams, 0)
  }

  close(): void {
    this.dialogRef.close()
  }
}
