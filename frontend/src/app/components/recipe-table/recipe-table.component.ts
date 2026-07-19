import {
  Component,
  inject,
  InputSignal,
  input,
  Signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core'
import { NgClass } from '@angular/common'
import { RecipeModel } from '../../models/RecipeModel'
import { RecipeStore } from '../../api/stores/recipe.store'
import { FoodModel } from '../../models/FoodModel'

@Component({
  selector: 'app-recipe-table',
  standalone: true,
  imports: [],
  templateUrl: './recipe-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class RecipeTableComponent {
  private readonly recipeStore = inject(RecipeStore)
  data: Signal<RecipeModel[]> = computed(() => this.recipeStore.recipes())
  columnHeaders: InputSignal<string[]> = input.required()

  delete(id: string) {
    this.recipeStore.delete(id)
  }

  listFoods(foods: FoodModel[]): string {
    return foods.map((food: FoodModel): string => food.name).join(', ')
  }

  addGrams(foods: FoodModel[]) {
    let gram = 0
    foods.forEach((food: FoodModel) => {
      gram = gram + food.grams
      console.log(gram)
    })
    return gram
  }

  addCalories(foods: FoodModel[]) {
    let calory = 0
    foods.forEach((food: FoodModel) => {
      calory = calory + food.calory
    })
    return calory
  }
}
