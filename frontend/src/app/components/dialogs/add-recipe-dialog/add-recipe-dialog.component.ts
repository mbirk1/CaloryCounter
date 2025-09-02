import { Component, inject } from '@angular/core'
import { Dialog, DIALOG_DATA, DialogRef } from '@angular/cdk/dialog'
import { TextInputComponent } from '../../inputs/text-input/text-input.component'
import { ButtonComponent } from '../../button/button.component'
import { LabelComponent } from '../../label/label.component'
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'
import { FormFactoryService } from '../../../services/factory/form.factory'
import { NumberInputComponent } from '../../inputs/number-input/number-input.component'
import { FoodStore } from '../../../api/stores/food.store'
import { RecipeStore } from '../../../api/stores/recipe.store'
import { CheckboxComponent } from '../../checkbox/checkbox.component'

@Component({
  selector: 'app-add-food-dialog',
  imports: [
    TextInputComponent,
    ButtonComponent,
    LabelComponent,
    ReactiveFormsModule,
    NumberInputComponent,
    CheckboxComponent,
  ],
  templateUrl: './add-recipe-dialog.component.html',
  styles: '',
})
export class AddRecipeDialogComponent extends Dialog {
  data = inject(DIALOG_DATA)
  recipe: FormGroup

  constructor(
    private formFactory: FormFactoryService,
    private recipeStore: RecipeStore,
  ) {
    super()
    this.recipe = this.formFactory
      .create()
      .control('name', '', [Validators.required, Validators.minLength(3)])
      .control('grams', '0', [Validators.required, Validators.minLength(1)])
      .control('calory', '0', [Validators.required, Validators.minLength(2)])
      .build()
  }

  onSubmit() {
    this.recipeStore.save(this.recipe.value)
  }

  addGrams(): number {
    return 0
  }
}
