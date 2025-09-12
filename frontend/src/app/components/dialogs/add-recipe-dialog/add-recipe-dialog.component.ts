import { Component, effect, inject } from '@angular/core'
import { Dialog, DIALOG_DATA, DialogRef } from '@angular/cdk/dialog'
import { TextInputComponent } from '../../inputs/text-input/text-input.component'
import { ButtonComponent } from '../../button/button.component'
import { LabelComponent } from '../../label/label.component'
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms'
import {
  DynamicFormBuilder,
  FormFactoryService,
} from '../../../services/factory/form.factory'
import { NumberInputComponent } from '../../inputs/number-input/number-input.component'
import { FoodStore } from '../../../api/stores/food.store'
import { RecipeStore } from '../../../api/stores/recipe.store'
import { CheckboxComponent } from '../../checkbox/checkbox.component'
import { FoodModel } from '../../../models/FoodModel'
import { RecipeModel } from '../../../models/RecipeModel'

@Component({
  selector: 'app-add-food-dialog',
  imports: [
    TextInputComponent,
    ButtonComponent,
    LabelComponent,
    ReactiveFormsModule,
    CheckboxComponent,
  ],
  templateUrl: './add-recipe-dialog.component.html',
  styles: '',
})
export class AddRecipeDialogComponent extends Dialog {
  data = inject(DIALOG_DATA)
  recipe: FormGroup
  foodStore: FoodStore = inject(FoodStore)
  foods: FoodModel[] = this.foodStore.foods()
  recipeStore: RecipeStore = inject(RecipeStore)
  grams = 0

  constructor(private formFactory: FormFactoryService) {
    super()
    let dynamic: DynamicFormBuilder = this.formFactory
      .create()
      .control('name', '', [Validators.required, Validators.minLength(3)])
    for (let item of this.foods) {
      dynamic.control(item.name, false)
    }
    this.recipe = dynamic.build()
  }

  onSubmit() {
    let foods: FoodModel[] = this.foods.filter((food: FoodModel): boolean => {
      const control = this.recipe.get(food.name)
      return control?.value === true
    })

    let recipe: RecipeModel = {
      name: this.recipe.get('name')?.value,
      foods: foods,
    }

    this.recipeStore.save(recipe)
  }

  totalGrams(): number {
    return this.foods.reduce((sum, food) => {
      const selected = this.recipe.get(food.name)?.value
      return selected ? sum + food.grams : sum
    }, 0)
  }
}
