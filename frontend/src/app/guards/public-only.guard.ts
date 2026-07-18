import { inject } from '@angular/core'
import { CanActivateFn, Router } from '@angular/router'
import { AuthStore } from '../api/stores/auth.store'

// Keeps already logged in users away from "/", "/login" and "/register".
export const publicOnlyGuard: CanActivateFn = async () => {
  const authStore = inject(AuthStore)
  const router = inject(Router)

  await authStore.bootstrap()

  if (authStore.isAuthenticated()) {
    return router.createUrlTree([
      authStore.profileCompleted() ? '/dashboard' : '/onboarding',
    ])
  }
  return true
}
