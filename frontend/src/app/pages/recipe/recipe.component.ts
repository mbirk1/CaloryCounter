import { Component, inject, ChangeDetectionStrategy } from '@angular/core'
import { ButtonComponent } from '../../components/button/button.component'
import { RecipeService } from '../../services/recipe.service'
import { RecipeTableComponent } from '../../components/recipe-table/recipe-table.component'
import { Dialog } from '@angular/cdk/dialog'
import { Overlay } from '@angular/cdk/overlay'
import { AddRecipeDialogComponent } from '../../components/dialogs/add-recipe-dialog/add-recipe-dialog.component'
import { FoodStore } from '../../api/stores/food.store'

@Component({
  selector: 'app-recipe',
  imports: [ButtonComponent, RecipeTableComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './recipe.component.html',
})
export class RecipeComponent {
  private readonly dialog = inject(Dialog)
  private readonly overlay = inject(Overlay)
  recipeService: RecipeService = inject(RecipeService)
  foodStore: FoodStore = inject(FoodStore)

  handleRecipeDialog() {
    this.foodStore.load()
    // Docked to the right edge, spanning the full viewport height - a wide drawer rather than
    // a centered modal, per explicit user preference (a recipe's food search + live nutrition
    // preview need more room than a small centered dialog comfortably gives).
    const positionStrategy = this.overlay
      .position()
      .global()
      .right('0')
      .top('0')
    this.dialog.open(AddRecipeDialogComponent, {
      positionStrategy,
      width: '75vw',
      height: '100vh',
    })
  }
}
