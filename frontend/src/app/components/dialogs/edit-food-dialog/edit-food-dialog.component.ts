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
