import { Component, computed, input, InputSignal, Signal } from '@angular/core'
import { FoodModel } from '../../models/FoodModel'
import { FoodStore } from '../../api/stores/food.store'
import { NgClass } from '@angular/common'
import { RecipeModel } from '../../models/RecipeModel'
import { RecipeStore } from '../../api/stores/recipe.store'

@Component({
  selector: 'app-food-table',
  standalone: true,
  imports: [NgClass],
  templateUrl: './food-table.component.html',
  styles: '',
})
export class FoodTableComponent {
  data: Signal<FoodModel[]> = computed(() => this.foodStore.foods())
  recipes: Signal<RecipeModel[]> = computed(() => this.recipeStore.recipes())
  columnHeaders: InputSignal<string[]> = input.required()

  constructor(
    private foodStore: FoodStore,
    private recipeStore: RecipeStore,
  ) {}

  delete(id: string) {
    this.foodStore.delete(id)
  }

  isInARecipe(foodId: string) {
    return this.recipes().some((recipe: RecipeModel) =>
      recipe.foods.some((food: FoodModel) => food.uuid === foodId),
    )
  }
}
