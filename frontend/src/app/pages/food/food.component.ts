import {Component, inject, OnInit} from '@angular/core';
import {ButtonComponent} from '../../components/button/button.component';
import {FoodService} from '../../services/food.service';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {faPen, faTrash} from '@fortawesome/free-solid-svg-icons';
import {FoodTableComponent} from '../../components/food-table/food-table.component';

@Component({
  selector: 'app-food',
  imports: [
    ButtonComponent,
    FontAwesomeModule,
    FoodTableComponent
  ],
  templateUrl: './food.component.html',
  styleUrl: './food.component.css'
})
export class FoodComponent implements OnInit {

  foodService: FoodService = inject(FoodService);
  foodResource = this.foodService.getFood();

  ngOnInit(): void {
    this.foodService.reload();
  }

  editFood(uuid: string) {

  }

  deleteFood(foodId: string) {

  }

  protected readonly faTrash = faTrash;
  protected readonly faPen = faPen;
}
