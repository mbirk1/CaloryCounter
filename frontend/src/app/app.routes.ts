import { Routes } from '@angular/router'
import { PublicLayoutComponent } from './layouts/public-layout/public-layout.component'
import { AppLayoutComponent } from './layouts/app-layout/app-layout.component'
import { LandingComponent } from './pages/landing/landing.component'
import { LoginComponent } from './pages/login/login.component'
import { RegisterComponent } from './pages/register/register.component'
import { OnboardingComponent } from './pages/onboarding/onboarding.component'
import { DashboardComponent } from './pages/dashboard/dashboard.component'
import { FoodComponent } from './pages/food/food.component'
import { RecipeComponent } from './pages/recipe/recipe.component'
import { publicOnlyGuard } from './guards/public-only.guard'
import { authGuard } from './guards/auth.guard'
import { profileCompleteGuard } from './guards/profile-complete.guard'
import { onboardingGuard } from './guards/onboarding.guard'

export const routes: Routes = [
  {
    path: '',
    component: PublicLayoutComponent,
    canActivate: [publicOnlyGuard],
    children: [
      { path: '', component: LandingComponent, pathMatch: 'full' },
      { path: 'login', component: LoginComponent },
      { path: 'register', component: RegisterComponent },
    ],
  },
  {
    path: '',
    component: AppLayoutComponent,
    children: [
      {
        path: 'onboarding',
        component: OnboardingComponent,
        canActivate: [onboardingGuard],
      },
      {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [authGuard, profileCompleteGuard],
      },
      {
        path: 'food',
        component: FoodComponent,
        canActivate: [authGuard, profileCompleteGuard],
      },
      {
        path: 'recipe',
        component: RecipeComponent,
        canActivate: [authGuard, profileCompleteGuard],
      },
    ],
  },
  { path: '**', redirectTo: '' },
]
