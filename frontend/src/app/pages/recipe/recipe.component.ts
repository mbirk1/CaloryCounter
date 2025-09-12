import { Component, inject } from '@angular/core'
import { ButtonComponent } from '../../components/button/button.component'
import { RecipeService } from '../../services/recipe.service'
import { RecipeTableComponent } from '../../components/recipe-table/recipe-table.component'
import { Dialog } from '@angular/cdk/dialog'
import { AddRecipeDialogComponent } from '../../components/dialogs/add-recipe-dialog/add-recipe-dialog.component'
import { FoodStore } from '../../api/stores/food.store'

@Component({
  selector: 'app-recipe',
  imports: [ButtonComponent, RecipeTableComponent],
  templateUrl: './recipe.component.html',
  styleUrl: './recipe.component.css',
})
export class RecipeComponent {
  recipeService: RecipeService = inject(RecipeService)
  foodStore: FoodStore = inject(FoodStore)

  constructor(private dialog: Dialog) {}

  handleRecipeDialog() {
    this.foodStore.load()
    const dialogRef = this.dialog.open(AddRecipeDialogComponent, {})
  }
}
