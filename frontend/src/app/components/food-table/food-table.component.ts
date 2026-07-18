import { Component, computed, input, InputSignal, Signal } from '@angular/core'
import { FoodModel } from '../../models/FoodModel'
import { FoodStore } from '../../api/stores/food.store'
import { RecipeModel } from '../../models/RecipeModel'
import { RecipeStore } from '../../api/stores/recipe.store'
import { Dialog } from '@angular/cdk/dialog'
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome'
import { faEye, faPen, faTrash } from '@fortawesome/free-solid-svg-icons'
import { FoodDetailDialogComponent } from '../dialogs/food-detail-dialog/food-detail-dialog.component'
import { PaginationComponent } from '../pagination/pagination.component'

@Component({
  selector: 'app-food-table',
  standalone: true,
  imports: [FontAwesomeModule, PaginationComponent],
  templateUrl: './food-table.component.html',
  styles: '',
})
export class FoodTableComponent {
  data: Signal<FoodModel[]> = computed(() => this.foodStore.foods())
  recipes: Signal<RecipeModel[]> = computed(() => this.recipeStore.recipes())
  columnHeaders: InputSignal<string[]> = input.required()

  currentPage: Signal<number> = computed(() => this.foodStore.currentPage())
  totalPages: Signal<number> = computed(() => this.foodStore.totalPages())

  protected readonly faEye = faEye
  protected readonly faTrash = faTrash
  protected readonly faPen = faPen

  constructor(
    private foodStore: FoodStore,
    private recipeStore: RecipeStore,
    private dialog: Dialog,
  ) {}

  delete(id: string) {
    this.foodStore.delete(id)
  }

  isInARecipe(foodId: string) {
    return this.recipes().some((recipe: RecipeModel) =>
      recipe.foods.some((food: FoodModel) => food.uuid === foodId),
    )
  }

  openDetails(food: FoodModel): void {
    this.dialog.open(FoodDetailDialogComponent, { data: food })
  }

  previousPage(): void {
    this.foodStore.previousPage()
  }

  nextPage(): void {
    this.foodStore.nextPage()
  }
}
