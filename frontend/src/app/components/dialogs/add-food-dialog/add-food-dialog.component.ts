import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  signal,
  Signal,
  WritableSignal,
} from '@angular/core'
import { toSignal } from '@angular/core/rxjs-interop'
import { Dialog, DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { TextInputComponent } from '../../inputs/text-input/text-input.component'
import { ButtonComponent } from '../../button/button.component'
import { LabelComponent } from '../../label/label.component'
import { CardComponent } from '../../card/card.component'
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'
import { FormFactoryService } from '../../../services/factory/form.factory'
import { NumberInputComponent } from '../../inputs/number-input/number-input.component'
import {
  SelectComponent,
  SelectOption,
} from '../../inputs/select/select.component'
import { FoodStore } from '../../../api/stores/food.store'
import { FoodModel } from '../../../models/FoodModel'

@Component({
  selector: 'app-add-food-dialog',
  imports: [
    TextInputComponent,
    ButtonComponent,
    LabelComponent,
    CardComponent,
    ReactiveFormsModule,
    NumberInputComponent,
    SelectComponent,
  ],
  templateUrl: './add-food-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class AddFoodDialogComponent extends Dialog {
  data = inject(DIALOG_DATA)
  food: FormGroup
  private readonly formFactory = inject(FormFactoryService)
  private readonly foodStore = inject(FoodStore)
  private readonly dialogRef = inject(DialogRef<boolean>)

  protected readonly dietOptions: SelectOption[] = [
    { value: 'UNKNOWN', label: 'Unbekannt' },
    { value: 'VEGAN', label: 'Vegan' },
    { value: 'VEGETARIAN', label: 'Vegetarisch' },
    { value: 'NON_VEGETARIAN', label: 'Nicht vegetarisch' },
  ]

  // Drives the live image preview in the "Weitere Angaben" section (FR-5) - a signal rather
  // than reading the FormControl's value directly in the template, since a nested custom form
  // control's value changes don't reliably trigger this OnPush component's own change
  // detection on their own.
  protected readonly imageUrlPreview: Signal<string>
  protected readonly imageLoadFailed: WritableSignal<boolean> = signal(false)

  constructor() {
    super()
    this.food = this.formFactory
      .create()
      .control('name', '', [Validators.required, Validators.minLength(3)])
      .control('grams', '0', [Validators.required, Validators.minLength(1)])
      .control('calory', '0', [Validators.required, Validators.minLength(2)])
      .control('diet', 'UNKNOWN', [])
      .control('brand', '', [])
      .control('category', '', [])
      .control('imageUrl', '', [])
      .control('fat', '', [])
      .control('saturatedFat', '', [])
      .control('carbohydrates', '', [])
      .control('sugar', '', [])
      .control('fiber', '', [])
      .control('protein', '', [])
      .control('salt', '', [])
      .control('sodium', '', [])
      .control('vitaminA', '', [])
      .control('vitaminD', '', [])
      .control('vitaminE', '', [])
      .control('vitaminK', '', [])
      .control('vitaminB1', '', [])
      .control('vitaminB2', '', [])
      .control('vitaminB6', '', [])
      .control('vitaminB12', '', [])
      .control('vitaminC', '', [])
      .control('folate', '', [])
      .control('potassium', '', [])
      .control('calcium', '', [])
      .control('magnesium', '', [])
      .control('iron', '', [])
      .control('zinc', '', [])
      .control('iodine', '', [])
      .build()

    this.imageUrlPreview = toSignal(this.food.get('imageUrl')!.valueChanges, {
      initialValue: '',
    })

    // A newly typed URL deserves a fresh load attempt rather than staying hidden because a
    // previous, different URL failed to load.
    effect(() => {
      this.imageUrlPreview()
      this.imageLoadFailed.set(false)
    })
  }

  async onSubmit(): Promise<void> {
    if (this.food.invalid) {
      return
    }
    const value = this.food.value
    const payload: FoodModel = {
      name: value.name,
      calory: Number(value.calory),
      grams: Number(value.grams),
      diet: value.diet,
      brand: this.textOrUndefined(value.brand),
      category: this.textOrUndefined(value.category),
      imageUrl: this.textOrUndefined(value.imageUrl),
      fat: this.numberOrUndefined(value.fat),
      saturatedFat: this.numberOrUndefined(value.saturatedFat),
      carbohydrates: this.numberOrUndefined(value.carbohydrates),
      sugar: this.numberOrUndefined(value.sugar),
      fiber: this.numberOrUndefined(value.fiber),
      protein: this.numberOrUndefined(value.protein),
      salt: this.numberOrUndefined(value.salt),
      sodium: this.numberOrUndefined(value.sodium),
      vitaminA: this.numberOrUndefined(value.vitaminA),
      vitaminD: this.numberOrUndefined(value.vitaminD),
      vitaminE: this.numberOrUndefined(value.vitaminE),
      vitaminK: this.numberOrUndefined(value.vitaminK),
      vitaminB1: this.numberOrUndefined(value.vitaminB1),
      vitaminB2: this.numberOrUndefined(value.vitaminB2),
      vitaminB6: this.numberOrUndefined(value.vitaminB6),
      vitaminB12: this.numberOrUndefined(value.vitaminB12),
      vitaminC: this.numberOrUndefined(value.vitaminC),
      folate: this.numberOrUndefined(value.folate),
      potassium: this.numberOrUndefined(value.potassium),
      calcium: this.numberOrUndefined(value.calcium),
      magnesium: this.numberOrUndefined(value.magnesium),
      iron: this.numberOrUndefined(value.iron),
      zinc: this.numberOrUndefined(value.zinc),
      iodine: this.numberOrUndefined(value.iodine),
    }

    const success = await this.foodStore.save(payload)
    if (success) {
      this.dialogRef.close(true)
    }
  }

  onImageError(): void {
    this.imageLoadFailed.set(true)
  }

  private textOrUndefined(value: string): string | undefined {
    return value === '' ? undefined : value
  }

  private numberOrUndefined(value: string): number | undefined {
    return value === undefined || value === null || value === ''
      ? undefined
      : Number(value)
  }
}
