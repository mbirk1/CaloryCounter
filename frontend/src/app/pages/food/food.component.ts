import {
  Component,
  inject,
  OnInit,
  ChangeDetectionStrategy,
} from '@angular/core'
import { ButtonComponent } from '../../components/button/button.component'
import { FoodService } from '../../services/food.service'
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome'
import { faPen, faTrash } from '@fortawesome/free-solid-svg-icons'
import { FoodTableComponent } from '../../components/food-table/food-table.component'
import { Dialog } from '@angular/cdk/dialog'
import { AddFoodDialogComponent } from '../../components/dialogs/add-food-dialog/add-food-dialog.component'

@Component({
  selector: 'app-food',
  imports: [ButtonComponent, FontAwesomeModule, FoodTableComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './food.component.html',
})
export class FoodComponent implements OnInit {
  private readonly dialog = inject(Dialog)
  foodService: FoodService = inject(FoodService)

  ngOnInit(): void {
    this.foodService.reload()
  }

  editFood(uuid: string) {}

  deleteFood(foodId: string) {}

  handleFoodDialog() {
    const dialogRef = this.dialog.open(AddFoodDialogComponent, {})
  }

  protected readonly faTrash = faTrash
  protected readonly faPen = faPen
}
