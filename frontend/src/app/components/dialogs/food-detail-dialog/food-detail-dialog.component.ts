import { ChangeDetectionStrategy, Component, inject } from '@angular/core'
import { Dialog, DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import {
  faDumbbell,
  faDroplet,
  faCubesStacked,
  faFire,
  faFlask,
  faFlaskVial,
  faLeaf,
  faWheatAwn,
  faXmark,
} from '@fortawesome/free-solid-svg-icons'
import { CardComponent } from '../../card/card.component'
import { DietBadgeComponent } from '../../diet-badge/diet-badge.component'
import { FoodModel } from '../../../models/FoodModel'

interface NutrientRow {
  label: string
  value: number | null | undefined
  icon: IconDefinition
}

const SOURCE_LABELS: Record<string, string> = {
  OPENFOODFACTS: 'Aus OpenFoodFacts importiert',
  MANUAL: 'Manuell angelegt',
}

@Component({
  selector: 'app-food-detail-dialog',
  imports: [CardComponent, DietBadgeComponent, FontAwesomeModule],
  templateUrl: './food-detail-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class FoodDetailDialogComponent extends Dialog {
  data: FoodModel = inject(DIALOG_DATA)
  private readonly dialogRef = inject(DialogRef)

  protected readonly faXmark = faXmark
  protected readonly faFire = faFire

  protected readonly sourceLabel: string | undefined = this.data.source
    ? SOURCE_LABELS[this.data.source]
    : undefined

  // Every value here is already expressed per 100g (matching the OpenFoodFacts CSV columns
  // they're imported from), so a bar capped at 100% doubles as a "share of 100g" reading -
  // no need to compare against the other rows' values to make the bars meaningful.
  protected readonly nutrientRows: NutrientRow[] = [
    { label: 'Fett', value: this.data.fat, icon: faDroplet },
    {
      label: 'davon gesättigt',
      value: this.data.saturatedFat,
      icon: faDroplet,
    },
    {
      label: 'Kohlenhydrate',
      value: this.data.carbohydrates,
      icon: faWheatAwn,
    },
    { label: 'davon Zucker', value: this.data.sugar, icon: faCubesStacked },
    { label: 'Ballaststoffe', value: this.data.fiber, icon: faLeaf },
    { label: 'Eiweiß', value: this.data.protein, icon: faDumbbell },
    { label: 'Salz', value: this.data.salt, icon: faFlask },
    { label: 'Natrium', value: this.data.sodium, icon: faFlaskVial },
  ]

  close(): void {
    this.dialogRef.close()
  }

  barWidth(value: number | null | undefined): number {
    return value == null ? 0 : Math.min(100, Math.max(0, value))
  }

  unit(value: number | null | undefined): string {
    return value == null ? '' : ' g'
  }
}
