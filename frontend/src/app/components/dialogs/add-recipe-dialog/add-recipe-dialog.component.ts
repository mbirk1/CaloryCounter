import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  OnDestroy,
  signal,
  ViewChild,
  WritableSignal,
} from '@angular/core'
import { Dialog, DIALOG_DATA, DialogRef } from '@angular/cdk/dialog'
import { TextInputComponent } from '../../inputs/text-input/text-input.component'
import { ButtonComponent } from '../../button/button.component'
import { LabelComponent } from '../../label/label.component'
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms'
import { debounceTime, distinctUntilChanged, Subscription } from 'rxjs'
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome'
import { faTrash, faXmark } from '@fortawesome/free-solid-svg-icons'
import { NumberInputComponent } from '../../inputs/number-input/number-input.component'
import { DietBadgeComponent } from '../../diet-badge/diet-badge.component'
import { RecipeNutrientSummaryComponent } from '../../recipe-nutrient-summary/recipe-nutrient-summary.component'
import { FoodStore } from '../../../api/stores/food.store'
import { RecipeStore } from '../../../api/stores/recipe.store'
import { FoodModel } from '../../../models/FoodModel'
import {
  CreateRecipePayload,
  RecipeFoodEntryPayload,
} from '../../../models/RecipeModel'

const SEARCH_DEBOUNCE_MS = 300

// Least strict (weakest) diet wins, mirroring the backend's Diet.STRICTNESS_ORDER - this is a
// client-side, JS-float estimate for the live preview only, not the authoritative result. The
// backend recomputes everything from scratch (in BigDecimal) on create, so small rounding
// differences here are harmless.
const DIET_STRICTNESS: Record<string, number> = {
  VEGAN: 0,
  VEGETARIAN: 1,
  NON_VEGETARIAN: 2,
  UNKNOWN: 3,
}

interface RecipeEntryFormValue {
  foodId: string
  name: string
  amountGrams: string
}

interface LiveNutritionSummary {
  totalCalories: number
  totalFat: number
  totalSaturatedFat: number
  totalCarbohydrates: number
  totalSugar: number
  totalFiber: number
  totalProtein: number
  totalSalt: number
  totalSodium: number
  caloriesPerServing: number
  fatPerServing: number
  saturatedFatPerServing: number
  carbohydratesPerServing: number
  sugarPerServing: number
  fiberPerServing: number
  proteinPerServing: number
  saltPerServing: number
  sodiumPerServing: number
  diet: string
  servings: number
  totalAmountGrams: number
}

function positiveNumberValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = Number(control.value)
    return control.value !== '' && value > 0 ? null : { positive: true }
  }
}

function round2(value: number): number {
  return Math.round(value * 100) / 100
}

@Component({
  selector: 'app-add-recipe-dialog',
  imports: [
    TextInputComponent,
    ButtonComponent,
    LabelComponent,
    ReactiveFormsModule,
    NumberInputComponent,
    FontAwesomeModule,
    DietBadgeComponent,
    RecipeNutrientSummaryComponent,
  ],
  templateUrl: './add-recipe-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class AddRecipeDialogComponent extends Dialog implements OnDestroy {
  data = inject(DIALOG_DATA)
  recipe: FormGroup
  foodStore: FoodStore = inject(FoodStore)
  recipeStore: RecipeStore = inject(RecipeStore)
  private readonly fb = inject(FormBuilder)
  private readonly dialogRef = inject(DialogRef<boolean>)

  @ViewChild('searchInputHost', { read: ElementRef })
  private searchInputHost?: ElementRef<HTMLElement>

  protected readonly faTrash = faTrash
  protected readonly faXmark = faXmark
  readonly searchControl = new FormControl('')
  readonly searchResults: WritableSignal<FoodModel[]> = signal([])
  // Visible only briefly starts false so the drawer can transition in from off-screen - see
  // constructor. Not a signal-driven animations-package trigger, just a plain CSS transition
  // toggled by a class binding, consistent with this codebase's otherwise animation-free style.
  readonly visible: WritableSignal<boolean> = signal(false)
  private readonly searchSubscription: Subscription

  // Full food data for each selected entry, index-aligned with the `entries` FormArray - the
  // form only needs to carry `foodId`/`amountGrams` for submission, but the live nutrition
  // preview needs each food's calory/macro/diet data, which isn't otherwise available without
  // a second lookup.
  private selectedFoods: FoodModel[] = []

  constructor() {
    super()
    this.recipe = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      servings: ['1', [Validators.required, positiveNumberValidator()]],
      entries: this.fb.array([]),
    })

    this.searchSubscription = this.searchControl.valueChanges
      .pipe(debounceTime(SEARCH_DEBOUNCE_MS), distinctUntilChanged())
      .subscribe((value) => this.runSearch(value ?? ''))

    setTimeout(() => this.visible.set(true))
  }

  override ngOnDestroy(): void {
    this.searchSubscription.unsubscribe()
    super.ngOnDestroy()
  }

  get entries(): FormArray {
    return this.recipe.get('entries') as FormArray
  }

  addFood(food: FoodModel): void {
    this.entries.push(
      this.fb.group({
        foodId: [food.uuid],
        name: [food.name],
        amountGrams: ['100', [Validators.required, positiveNumberValidator()]],
      }),
    )
    this.selectedFoods.push(food)
    this.searchResults.set([])
    this.searchControl.setValue('')
    this.searchInputHost?.nativeElement.querySelector('input')?.focus()
  }

  removeFood(index: number): void {
    this.entries.removeAt(index)
    this.selectedFoods.splice(index, 1)
  }

  close(): void {
    this.dialogRef.close()
  }

  async onSubmit(): Promise<void> {
    if (this.recipe.invalid) {
      this.recipe.markAllAsTouched()
      return
    }
    const entries = this.entries.value as RecipeEntryFormValue[]
    const foods: RecipeFoodEntryPayload[] = entries.map((entry) => ({
      foodId: entry.foodId,
      amountGrams: Number(entry.amountGrams),
    }))

    // No client-side "at least one food" check - the backend rejects an empty list with a 406
    // that RecipeStore.save() already surfaces as a toast, so it doesn't need duplicating here.
    const payload: CreateRecipePayload = {
      name: this.recipe.get('name')?.value,
      servings: Number(this.recipe.get('servings')?.value),
      foods,
    }

    const success = await this.recipeStore.save(payload)
    if (success) {
      this.dialogRef.close(true)
    }
  }

  // Recomputed on every change-detection pass (not memoized) - cheap for the handful of
  // ingredients a recipe realistically has, and this is only ever an estimate anyway (the
  // backend recomputes the authoritative totals from scratch on create).
  nutritionSummary(): LiveNutritionSummary {
    const amounts = this.entries.controls.map(
      (control) => Number(control.get('amountGrams')?.value) || 0,
    )
    const servings = Math.max(
      1,
      Number(this.recipe.get('servings')?.value) || 1,
    )

    const totalCalories = this.sumProportional((food) => food.calory, amounts)
    const totalFat = this.sumProportional((food) => food.fat, amounts)
    const totalSaturatedFat = this.sumProportional(
      (food) => food.saturatedFat,
      amounts,
    )
    const totalCarbohydrates = this.sumProportional(
      (food) => food.carbohydrates,
      amounts,
    )
    const totalSugar = this.sumProportional((food) => food.sugar, amounts)
    const totalFiber = this.sumProportional((food) => food.fiber, amounts)
    const totalProtein = this.sumProportional((food) => food.protein, amounts)
    const totalSalt = this.sumProportional((food) => food.salt, amounts)
    const totalSodium = this.sumProportional((food) => food.sodium, amounts)

    return {
      totalCalories,
      totalFat,
      totalSaturatedFat,
      totalCarbohydrates,
      totalSugar,
      totalFiber,
      totalProtein,
      totalSalt,
      totalSodium,
      caloriesPerServing: round2(totalCalories / servings),
      fatPerServing: round2(totalFat / servings),
      saturatedFatPerServing: round2(totalSaturatedFat / servings),
      carbohydratesPerServing: round2(totalCarbohydrates / servings),
      sugarPerServing: round2(totalSugar / servings),
      fiberPerServing: round2(totalFiber / servings),
      proteinPerServing: round2(totalProtein / servings),
      saltPerServing: round2(totalSalt / servings),
      sodiumPerServing: round2(totalSodium / servings),
      diet: this.computeDiet(),
      servings,
      totalAmountGrams: amounts.reduce((sum, amount) => sum + amount, 0),
    }
  }

  private sumProportional(
    selector: (food: FoodModel) => number | null | undefined,
    amounts: number[],
  ): number {
    let total = 0
    this.selectedFoods.forEach((food, index) => {
      const value = selector(food)
      if (value == null) {
        return
      }
      const ratio = food.grams > 0 ? amounts[index] / food.grams : 0
      total += value * ratio
    })
    return round2(total)
  }

  private computeDiet(): string {
    return this.selectedFoods.reduce((worst, food) => {
      const current = food.diet ?? 'UNKNOWN'
      return DIET_STRICTNESS[current] > DIET_STRICTNESS[worst] ? current : worst
    }, 'VEGAN')
  }

  private async runSearch(query: string): Promise<void> {
    if (!query.trim()) {
      this.searchResults.set([])
      return
    }
    const results = await this.foodStore.searchFoods(query)
    const selectedIds = new Set(
      (this.entries.value as RecipeEntryFormValue[]).map(
        (entry) => entry.foodId,
      ),
    )
    this.searchResults.set(
      results.filter((food) => !selectedIds.has(food.uuid!)),
    )
  }
}
