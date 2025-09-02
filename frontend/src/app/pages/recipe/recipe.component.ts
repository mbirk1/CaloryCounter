import { Component, inject } from '@angular/core'
import { ButtonComponent } from '../../components/button/button.component'
import { RecipeService } from '../../services/recipe.service'
import { RecipeTableComponent } from '../../components/recipe-table/recipe-table.component'
import { Dialog } from '@angular/cdk/dialog'
import { AddRecipeDialogComponent } from '../../components/dialogs/add-recipe-dialog/add-recipe-dialog.component'

@Component({
  selector: 'app-recipe',
  imports: [ButtonComponent, RecipeTableComponent],
  templateUrl: './recipe.component.html',
  styleUrl: './recipe.component.css',
})
export class RecipeComponent {
  recipeService: RecipeService = inject(RecipeService)

  constructor(private dialog: Dialog) {}

  handleRecipeDialog() {
    const dialogRef = this.dialog.open(AddRecipeDialogComponent, {})
  }
}
