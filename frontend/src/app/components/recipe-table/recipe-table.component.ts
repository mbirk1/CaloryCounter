import { Component, InputSignal, input, Signal, computed } from '@angular/core'
import { NgClass } from '@angular/common'
import { RecipeModel } from '../../models/RecipeModel'
import { RecipeStore } from '../../api/stores/recipe.store'

@Component({
  selector: 'app-recipe-table',
  standalone: true,
  imports: [NgClass],
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
}
