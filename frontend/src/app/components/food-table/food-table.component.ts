import { Component, InputSignal, input } from '@angular/core';
import { faPen, faTrash } from '@fortawesome/free-solid-svg-icons';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'app-food-table',
  standalone: true,
  imports: [
    FaIconComponent,
    ButtonComponent
  ],
  templateUrl: './food-table.component.html',
  styles: ''
})
export class FoodTableComponent {
  data: InputSignal<any[]> = input.required();
  columnHeaders: InputSignal<string[]> = input.required();

  readonly faTrash = faTrash;
  readonly faPen = faPen;
}
