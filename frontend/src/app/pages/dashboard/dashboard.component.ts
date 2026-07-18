import { Component, computed, inject, Signal } from '@angular/core'
import { DecimalPipe } from '@angular/common'
import { RouterLink } from '@angular/router'
import { AuthStore } from '../../api/stores/auth.store'
import { FoodStore } from '../../api/stores/food.store'
import { RecipeStore } from '../../api/stores/recipe.store'
import { CardComponent } from '../../components/card/card.component'
import { UserModel } from '../../models/UserModel'

const ACTIVITY_LABELS: Record<string, string> = {
  SEDENTARY: 'Sitzend',
  LIGHTLY_ACTIVE: 'Leicht aktiv',
  MODERATELY_ACTIVE: 'Mäßig aktiv',
  VERY_ACTIVE: 'Sehr aktiv',
  EXTRA_ACTIVE: 'Extrem aktiv',
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DecimalPipe, CardComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
  authStore = inject(AuthStore)
  foodStore = inject(FoodStore)
  recipeStore = inject(RecipeStore)

  user: Signal<UserModel | null> = computed(() => this.authStore.user())

  bmi: Signal<number | null> = computed(() => {
    const current = this.user()
    if (!current?.height || !current?.weight) {
      return null
    }
    const heightInMeters = current.height / 100
    return current.weight / (heightInMeters * heightInMeters)
  })

  activityLabel: Signal<string> = computed(() => {
    const level = this.user()?.activityLevel
    return level ? ACTIVITY_LABELS[level] : '–'
  })

  foodCount: Signal<number> = computed(() => this.foodStore.foods().length)
  recipeCount: Signal<number> = computed(
    () => this.recipeStore.recipes().length,
  )
  hasData: Signal<boolean> = computed(
    () => this.foodCount() > 0 || this.recipeCount() > 0,
  )
}
