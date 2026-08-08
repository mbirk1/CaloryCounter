import { inject } from '@angular/core'
import { CanActivateFn, Router } from '@angular/router'
import { AuthStore } from '../api/stores/auth.store'

// Keeps unauthenticated users off /dashboard, /food and /recipe.
export const authGuard: CanActivateFn = async () => {
  const authStore = inject(AuthStore)
  const router = inject(Router)

  await authStore.bootstrap()

  if (!authStore.isAuthenticated()) {
    return router.createUrlTree(['/login'])
  }
  return true
}
