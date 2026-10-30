import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  InputSignal,
  Signal,
} from '@angular/core'
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import {
  faCubesStacked,
  faDroplet,
  faDumbbell,
  faFire,
  faFlask,
  faFlaskVial,
  faLeaf,
  faWheatAwn,
} from '@fortawesome/free-solid-svg-icons'
import { CardComponent } from '../card/card.component'

interface NutrientRow {
  label: string
  value: number | null | undefined
  icon: IconDefinition
}

// Presentational-only: renders the "total + per-serving calories" block and the per-serving
// macro bars. Shared by RecipeDetailDialogComponent (a saved recipe's authoritative,
// server-computed values) and the add-recipe drawer's live preview (a client-side estimate
// recomputed as the user edits) - same markup, different data sources.
@Component({
  selector: 'app-recipe-nutrient-summary',
  imports: [CardComponent, FontAwesomeModule],
  templateUrl: './recipe-nutrient-summary.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class RecipeNutrientSummaryComponent {
  totalCalories: InputSignal<number> = input(0)
  caloriesPerServing: InputSignal<number> = input(0)
  servings: InputSignal<number> = input(1)
  totalAmountGrams: InputSignal<number> = input(0)
  fatPerServing: InputSignal<number | null | undefined> = input<
    number | null | undefined
  >()
  saturatedFatPerServing: InputSignal<number | null | undefined> = input<
    number | null | undefined
  >()
  carbohydratesPerServing: InputSignal<number | null | undefined> = input<
    number | null | undefined
  >()
  sugarPerServing: InputSignal<number | null | undefined> = input<
    number | null | undefined
  >()
  fiberPerServing: InputSignal<number | null | undefined> = input<
    number | null | undefined
  >()
  proteinPerServing: InputSignal<number | null | undefined> = input<
    number | null | undefined
  >()
  saltPerServing: InputSignal<number | null | undefined> = input<
    number | null | undefined
  >()
  sodiumPerServing: InputSignal<number | null | undefined> = input<
    number | null | undefined
  >()

  protected readonly faFire = faFire

  protected readonly nutrientRows: Signal<NutrientRow[]> = computed(() => [
    { label: 'Fett', value: this.fatPerServing(), icon: faDroplet },
    {
      label: 'davon gesättigt',
      value: this.saturatedFatPerServing(),
      icon: faDroplet,
    },
    {
      label: 'Kohlenhydrate',
      value: this.carbohydratesPerServing(),
      icon: faWheatAwn,
    },
    {
      label: 'davon Zucker',
      value: this.sugarPerServing(),
      icon: faCubesStacked,
    },
    { label: 'Ballaststoffe', value: this.fiberPerServing(), icon: faLeaf },
    { label: 'Eiweiß', value: this.proteinPerServing(), icon: faDumbbell },
    { label: 'Salz', value: this.saltPerServing(), icon: faFlask },
    { label: 'Natrium', value: this.sodiumPerServing(), icon: faFlaskVial },
  ])

  barWidth(value: number | null | undefined): number {
    return value == null ? 0 : Math.min(100, Math.max(0, value))
  }

  unit(value: number | null | undefined): string {
    return value == null ? '' : ' g'
  }
}
