import {
  Component,
  inject,
  InputSignal,
  input,
  Signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core'
import { Dialog } from '@angular/cdk/dialog'
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome'
import { faEye } from '@fortawesome/free-solid-svg-icons'
import { DietBadgeComponent } from '../diet-badge/diet-badge.component'
import { RecipeModel } from '../../models/RecipeModel'
import { RecipeStore } from '../../api/stores/recipe.store'
import { RecipeDetailDialogComponent } from '../dialogs/recipe-detail-dialog/recipe-detail-dialog.component'

@Component({
  selector: 'app-recipe-table',
  standalone: true,
  imports: [FontAwesomeModule, DietBadgeComponent],
  templateUrl: './recipe-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: '',
})
export class RecipeTableComponent {
  private readonly recipeStore = inject(RecipeStore)
  private readonly dialog = inject(Dialog)
  data: Signal<RecipeModel[]> = computed(() => this.recipeStore.recipes())
  columnHeaders: InputSignal<string[]> = input.required()

  protected readonly faEye = faEye

  delete(id: string) {
    this.recipeStore.delete(id)
  }

  openDetails(recipe: RecipeModel): void {
    this.dialog.open(RecipeDetailDialogComponent, { data: recipe })
  }
}
