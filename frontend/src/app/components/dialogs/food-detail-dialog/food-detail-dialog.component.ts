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
  unit: string
  // The value a bar at 100% width represents - for the macros this is simply 100 (they're
  // already per-100g grams, so the bar doubles as a "share of 100g" reading), but a micronutrient
  // has no such natural 100-based scale, so its bar is instead read against its own plausible
  // upper bound (matching Food#validatePlausibility on the backend).
  max: number
}

const SOURCE_LABELS: Record<string, string> = {
  OPENFOODFACTS: 'Aus OpenFoodFacts importiert',
  BLS: 'Aus dem Bundeslebensmittelschlüssel importiert',
  MANUAL: 'Manuell angelegt',
}

const MACRO_MAX = 100

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
    {
      label: 'Fett',
      value: this.data.fat,
      icon: faDroplet,
      unit: 'g',
      max: MACRO_MAX,
    },
    {
      label: 'davon gesättigt',
      value: this.data.saturatedFat,
      icon: faDroplet,
      unit: 'g',
      max: MACRO_MAX,
    },
    {
      label: 'Kohlenhydrate',
      value: this.data.carbohydrates,
      icon: faWheatAwn,
      unit: 'g',
      max: MACRO_MAX,
    },
    {
      label: 'davon Zucker',
      value: this.data.sugar,
      icon: faCubesStacked,
      unit: 'g',
      max: MACRO_MAX,
    },
    {
      label: 'Ballaststoffe',
      value: this.data.fiber,
      icon: faLeaf,
      unit: 'g',
      max: MACRO_MAX,
    },
    {
      label: 'Eiweiß',
      value: this.data.protein,
      icon: faDumbbell,
      unit: 'g',
      max: MACRO_MAX,
    },
    {
      label: 'Salz',
      value: this.data.salt,
      icon: faFlask,
      unit: 'g',
      max: MACRO_MAX,
    },
    {
      label: 'Natrium',
      value: this.data.sodium,
      icon: faFlaskVial,
      unit: 'g',
      max: MACRO_MAX,
    },
  ]

  // Bounds mirror Food#validatePlausibility on the backend - a bar at 100% width means "at the
  // plausible upper bound for this nutrient", not "100g/100mg/100µg" like the macro bars above.
  protected readonly microNutrientRows: NutrientRow[] = [
    {
      label: 'Vitamin A',
      value: this.data.vitaminA,
      icon: faLeaf,
      unit: 'µg',
      max: 30000,
    },
    {
      label: 'Vitamin D',
      value: this.data.vitaminD,
      icon: faLeaf,
      unit: 'µg',
      max: 500,
    },
    {
      label: 'Vitamin E',
      value: this.data.vitaminE,
      icon: faLeaf,
      unit: 'mg',
      max: 300,
    },
    {
      label: 'Vitamin K',
      value: this.data.vitaminK,
      icon: faLeaf,
      unit: 'µg',
      max: 3000,
    },
    {
      label: 'Vitamin B1',
      value: this.data.vitaminB1,
      icon: faLeaf,
      unit: 'mg',
      max: 30,
    },
    {
      label: 'Vitamin B2',
      value: this.data.vitaminB2,
      icon: faLeaf,
      unit: 'mg',
      max: 20,
    },
    {
      label: 'Vitamin B6',
      value: this.data.vitaminB6,
      icon: faLeaf,
      unit: 'µg',
      max: 10000,
    },
    {
      label: 'Vitamin B12',
      value: this.data.vitaminB12,
      icon: faLeaf,
      unit: 'µg',
      max: 200,
    },
    {
      label: 'Vitamin C',
      value: this.data.vitaminC,
      icon: faLeaf,
      unit: 'mg',
      max: 3000,
    },
    {
      label: 'Folat',
      value: this.data.folate,
      icon: faLeaf,
      unit: 'µg',
      max: 20000,
    },
    {
      label: 'Kalium',
      value: this.data.potassium,
      icon: faFlaskVial,
      unit: 'mg',
      max: 10000,
    },
    {
      label: 'Calcium',
      value: this.data.calcium,
      icon: faFlaskVial,
      unit: 'mg',
      max: 3000,
    },
    {
      label: 'Magnesium',
      value: this.data.magnesium,
      icon: faFlaskVial,
      unit: 'mg',
      max: 2000,
    },
    {
      label: 'Eisen',
      value: this.data.iron,
      icon: faFlaskVial,
      unit: 'mg',
      max: 150,
    },
    {
      label: 'Zink',
      value: this.data.zinc,
      icon: faFlaskVial,
      unit: 'mg',
      max: 150,
    },
    {
      label: 'Iod',
      value: this.data.iodine,
      icon: faFlaskVial,
      unit: 'µg',
      max: 5000,
    },
  ]

  protected readonly hasAnyMicronutrient: boolean = this.microNutrientRows.some(
    (row) => row.value != null,
  )

  close(): void {
    this.dialogRef.close()
  }

  barWidth(row: NutrientRow): number {
    return row.value == null
      ? 0
      : Math.min(100, Math.max(0, (row.value / row.max) * 100))
  }

  unit(row: NutrientRow): string {
    return row.value == null ? '' : ` ${row.unit}`
  }
}
