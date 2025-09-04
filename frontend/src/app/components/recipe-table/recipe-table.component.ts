import { Component, InputSignal, input, Signal, computed } from '@angular/core'
import { NgClass } from '@angular/common'
import { RecipeModel } from '../../models/RecipeModel'
import { RecipeStore } from '../../api/stores/recipe.store'
import { FoodModel } from '../../models/FoodModel'

@Component({
  selector: 'app-recipe-table',
  standalone: true,
  imports: [],
  templateUrl: './recipe-table.component.html',
  styles: '',
})
export class RecipeTableComponent {
  data: Signal<RecipeModel[]> = computed(() => this.recipeStore.recipes())
  columnHeaders: InputSignal<string[]> = input.required()

  constructor(private recipeStore: RecipeStore) {}

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
