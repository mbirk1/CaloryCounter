import { Routes } from '@angular/router';
import {DashboardComponent} from './pages/dashboard/dashboard.component';
import {FoodComponent} from './pages/food/food.component';
import {RecipeComponent} from './pages/recipe/recipe.component';

export const routes: Routes = [
  {
    path: '',
    component: DashboardComponent
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
  },
  {
    path: 'food',
    component: FoodComponent
  },
  {
    path: 'recipe',
    component: RecipeComponent,
  }
];
