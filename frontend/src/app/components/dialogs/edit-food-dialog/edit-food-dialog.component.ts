import { ChangeDetectionStrategy, Component, inject } from '@angular/core'
import { Dialog, DialogRef, DIALOG_DATA } from '@angular/cdk/dialog'
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'
import { TextInputComponent } from '../../inputs/text-input/text-input.component'
import { NumberInputComponent } from '../../inputs/number-input/number-input.component'
import {
  SelectComponent,
  SelectOption,
} from '../../inputs/select/select.component'
import { ButtonComponent } from '../../button/button.component'
import { LabelComponent } from '../../label/label.component'
import { CardComponent } from '../../card/card.component'
import { FormFactoryService } from '../../../services/factory/form.factory'
import { FoodStore } from '../../../api/stores/food.store'
import { FoodModel } from '../../../models/FoodModel'

@Component({
  selector: 'app-edit-food-dialog',
  imports: [
    TextInputComponent,
    NumberInputComponent,
    SelectComponent,
    ButtonComponent,
    LabelComponent,
    CardComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './edit-food-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class EditFoodDialogComponent extends Dialog {
  data: FoodModel = inject(DIALOG_DATA)
  private readonly dialogRef = inject(DialogRef<boolean>)
  private readonly formFactory = inject(FormFactoryService)
  private readonly foodStore = inject(FoodStore)

  protected readonly dietOptions: SelectOption[] = [
    { value: 'UNKNOWN', label: 'Unbekannt' },
    { value: 'VEGAN', label: 'Vegan' },
    { value: 'VEGETARIAN', label: 'Vegetarisch' },
    { value: 'NON_VEGETARIAN', label: 'Nicht vegetarisch' },
  ]

  food: FormGroup

  constructor() {
    super()
    this.food = this.formFactory
      .create()
      .control('name', this.data.name, [
        Validators.required,
        Validators.minLength(3),
      ])
      .control('grams', String(this.data.grams), [
        Validators.required,
        Validators.minLength(1),
      ])
      .control('calory', String(this.data.calory), [
        Validators.required,
        Validators.minLength(1),
      ])
      .control('diet', this.data.diet ?? 'UNKNOWN', [])
      .control('fat', this.numberOrEmpty(this.data.fat), [])
      .control('saturatedFat', this.numberOrEmpty(this.data.saturatedFat), [])
      .control('carbohydrates', this.numberOrEmpty(this.data.carbohydrates), [])
      .control('sugar', this.numberOrEmpty(this.data.sugar), [])
      .control('fiber', this.numberOrEmpty(this.data.fiber), [])
      .control('protein', this.numberOrEmpty(this.data.protein), [])
      .control('salt', this.numberOrEmpty(this.data.salt), [])
      .control('sodium', this.numberOrEmpty(this.data.sodium), [])
      .control('vitaminA', this.numberOrEmpty(this.data.vitaminA), [])
      .control('vitaminD', this.numberOrEmpty(this.data.vitaminD), [])
      .control('vitaminE', this.numberOrEmpty(this.data.vitaminE), [])
      .control('vitaminK', this.numberOrEmpty(this.data.vitaminK), [])
      .control('vitaminB1', this.numberOrEmpty(this.data.vitaminB1), [])
      .control('vitaminB2', this.numberOrEmpty(this.data.vitaminB2), [])
      .control('vitaminB6', this.numberOrEmpty(this.data.vitaminB6), [])
      .control('vitaminB12', this.numberOrEmpty(this.data.vitaminB12), [])
      .control('vitaminC', this.numberOrEmpty(this.data.vitaminC), [])
      .control('folate', this.numberOrEmpty(this.data.folate), [])
      .control('potassium', this.numberOrEmpty(this.data.potassium), [])
      .control('calcium', this.numberOrEmpty(this.data.calcium), [])
      .control('magnesium', this.numberOrEmpty(this.data.magnesium), [])
      .control('iron', this.numberOrEmpty(this.data.iron), [])
      .control('zinc', this.numberOrEmpty(this.data.zinc), [])
      .control('iodine', this.numberOrEmpty(this.data.iodine), [])
      .build()
  }

  async onSubmit(): Promise<void> {
    if (this.food.invalid) {
      return
    }
    const value = this.food.value
    const payload: FoodModel = {
      uuid: this.data.uuid,
      name: value.name,
      calory: Number(value.calory),
      grams: Number(value.grams),
      diet: value.diet,
      fat: this.emptyToUndefined(value.fat),
      saturatedFat: this.emptyToUndefined(value.saturatedFat),
      carbohydrates: this.emptyToUndefined(value.carbohydrates),
      sugar: this.emptyToUndefined(value.sugar),
      fiber: this.emptyToUndefined(value.fiber),
      protein: this.emptyToUndefined(value.protein),
      salt: this.emptyToUndefined(value.salt),
      sodium: this.emptyToUndefined(value.sodium),
      vitaminA: this.emptyToUndefined(value.vitaminA),
      vitaminD: this.emptyToUndefined(value.vitaminD),
      vitaminE: this.emptyToUndefined(value.vitaminE),
      vitaminK: this.emptyToUndefined(value.vitaminK),
      vitaminB1: this.emptyToUndefined(value.vitaminB1),
      vitaminB2: this.emptyToUndefined(value.vitaminB2),
      vitaminB6: this.emptyToUndefined(value.vitaminB6),
      vitaminB12: this.emptyToUndefined(value.vitaminB12),
      vitaminC: this.emptyToUndefined(value.vitaminC),
      folate: this.emptyToUndefined(value.folate),
      potassium: this.emptyToUndefined(value.potassium),
      calcium: this.emptyToUndefined(value.calcium),
      magnesium: this.emptyToUndefined(value.magnesium),
      iron: this.emptyToUndefined(value.iron),
      zinc: this.emptyToUndefined(value.zinc),
      iodine: this.emptyToUndefined(value.iodine),
    }

    const success = await this.foodStore.update(this.data.uuid!, payload)
    if (success) {
      this.dialogRef.close(true)
    }
  }

  private numberOrEmpty(value: number | undefined): string {
    return value === undefined || value === null ? '' : String(value)
  }

  private emptyToUndefined(value: string): number | undefined {
    return value === undefined || value === null || value === ''
      ? undefined
      : Number(value)
  }
}
