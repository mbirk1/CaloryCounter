import {
  Component,
  computed,
  inject,
  input,
  InputSignal,
  Signal,
  ChangeDetectionStrategy,
} from '@angular/core'
import { FoodModel } from '../../models/FoodModel'
import {
  FoodStore,
  SortDirection,
  SortField,
} from '../../api/stores/food.store'
import { RecipeModel } from '../../models/RecipeModel'
import { RecipeStore } from '../../api/stores/recipe.store'
import { Dialog } from '@angular/cdk/dialog'
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome'
import {
  faEye,
  faPen,
  faSort,
  faSortDown,
  faSortUp,
  faTrash,
} from '@fortawesome/free-solid-svg-icons'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { FoodDetailDialogComponent } from '../dialogs/food-detail-dialog/food-detail-dialog.component'
import { ConfirmDialogComponent } from '../dialogs/confirm-dialog/confirm-dialog.component'
import { EditFoodDialogComponent } from '../dialogs/edit-food-dialog/edit-food-dialog.component'
import { PaginationComponent } from '../pagination/pagination.component'

@Component({
  selector: 'app-food-table',
  standalone: true,
  imports: [FontAwesomeModule, PaginationComponent],
  templateUrl: './food-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class FoodTableComponent {
  private readonly foodStore = inject(FoodStore)
  private readonly recipeStore = inject(RecipeStore)
  private readonly dialog = inject(Dialog)
  data: Signal<FoodModel[]> = computed(() => this.foodStore.foods())
  recipes: Signal<RecipeModel[]> = computed(() => this.recipeStore.recipes())
  columnHeaders: InputSignal<string[]> = input.required()

  currentPage: Signal<number> = computed(() => this.foodStore.currentPage())
  totalPages: Signal<number> = computed(() => this.foodStore.totalPages())
  sort: Signal<SortField> = computed(() => this.foodStore.sort())
  direction: Signal<SortDirection> = computed(() => this.foodStore.direction())

  protected readonly faEye = faEye
  protected readonly faTrash = faTrash
  protected readonly faPen = faPen

  confirmDelete(food: FoodModel): void {
    if (this.foodStore.isDeleting(food.uuid!)) {
      return
    }
    const dialogRef = this.dialog.open<boolean>(ConfirmDialogComponent, {
      data: {
        title: 'Lebensmittel löschen',
        message:
          `Möchtest du "${food.name}" wirklich löschen? ` +
          'Diese Aktion kann nicht rückgängig gemacht werden.',
        confirmText: 'Löschen',
      },
    })
    dialogRef.closed.subscribe((confirmed) => {
      if (confirmed) {
        this.foodStore.delete(food.uuid!)
      }
    })
  }

  openEdit(food: FoodModel): void {
    this.dialog.open(EditFoodDialogComponent, { data: food })
  }

  isInARecipe(foodId: string) {
    return this.recipes().some((recipe: RecipeModel) =>
      recipe.foods.some((food: FoodModel) => food.uuid === foodId),
    )
  }

  isDeleting(foodId: string): boolean {
    return this.foodStore.isDeleting(foodId)
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

  onSort(field: SortField): void {
    this.foodStore.setSort(field)
  }

  ariaSortFor(field: SortField): 'ascending' | 'descending' | 'none' {
    if (this.sort() !== field) {
      return 'none'
    }
    return this.direction() === 'asc' ? 'ascending' : 'descending'
  }

  sortIconFor(field: SortField): IconDefinition {
    if (this.sort() !== field) {
      return faSort
    }
    return this.direction() === 'asc' ? faSortUp : faSortDown
  }
}
