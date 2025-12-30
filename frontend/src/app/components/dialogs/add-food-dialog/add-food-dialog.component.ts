import {
  Component,
  computed,
  effect,
  inject,
  signal,
  WritableSignal,
} from '@angular/core'
import { Dialog, DIALOG_DATA } from '@angular/cdk/dialog'
import { TextInputComponent } from '../../inputs/text-input/text-input.component'
import { ButtonComponent } from '../../button/button.component'
import { LabelComponent } from '../../label/label.component'
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'
import { FormFactoryService } from '../../../services/factory/form.factory'
import { NumberInputComponent } from '../../inputs/number-input/number-input.component'
import { FoodStore } from '../../../api/stores/food.store'

@Component({
  selector: 'app-add-food-dialog',
  imports: [
    TextInputComponent,
    ButtonComponent,
    LabelComponent,
    ReactiveFormsModule,
    NumberInputComponent,
  ],
  templateUrl: './add-food-dialog.component.html',
  styles: '',
})
export class AddFoodDialogComponent extends Dialog {
  data = inject(DIALOG_DATA)
  food: FormGroup
  foodName: WritableSignal<string> = signal('')

  constructor(
    private formFactory: FormFactoryService,
    private foodStore: FoodStore,
  ) {
    super()
    this.food = this.formFactory
      .create()
      .control('name', '', [Validators.required, Validators.minLength(3)])
      .control('grams', '0', [Validators.required, Validators.minLength(1)])
      .control('calory', '0', [Validators.required, Validators.minLength(2)])
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
