import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  signal,
  WritableSignal,
} from '@angular/core'
import { Dialog, DIALOG_DATA, DialogRef } from '@angular/cdk/dialog'
import { TextInputComponent } from '../../inputs/text-input/text-input.component'
import { ButtonComponent } from '../../button/button.component'
import { LabelComponent } from '../../label/label.component'
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'
import { FormFactoryService } from '../../../services/factory/form.factory'
import { NumberInputComponent } from '../../inputs/number-input/number-input.component'
import {
  SelectComponent,
  SelectOption,
} from '../../inputs/select/select.component'
import { FoodStore } from '../../../api/stores/food.store'

@Component({
  selector: 'app-add-food-dialog',
  imports: [
    TextInputComponent,
    ButtonComponent,
    LabelComponent,
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
  foodName: WritableSignal<string> = signal('')

  protected readonly dietOptions: SelectOption[] = [
    { value: 'UNKNOWN', label: 'Unbekannt' },
    { value: 'VEGAN', label: 'Vegan' },
    { value: 'VEGETARIAN', label: 'Vegetarisch' },
    { value: 'NON_VEGETARIAN', label: 'Nicht vegetarisch' },
  ]

  constructor() {
    super()
    this.food = this.formFactory
      .create()
      .control('name', '', [Validators.required, Validators.minLength(3)])
      .control('grams', '0', [Validators.required, Validators.minLength(1)])
      .control('calory', '0', [Validators.required, Validators.minLength(2)])
      .control('diet', 'UNKNOWN', [])
      .build()

    effect(() => {})
  }

  onSubmit() {
    this.foodStore.save(this.food.value)
  }

  onChange(event: Event) {
    this.foodName.set((event?.target as HTMLInputElement).value)
  }
}
