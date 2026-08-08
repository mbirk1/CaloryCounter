import { inject } from '@angular/core'
import { CanActivateFn, Router } from '@angular/router'
import { AuthStore } from '../api/stores/auth.store'

// /onboarding is only reachable while logged in and only as long as the profile is incomplete -
// once it is complete this redirects to /dashboard instead of showing the form again.
export const onboardingGuard: CanActivateFn = async () => {
  const authStore = inject(AuthStore)
  const router = inject(Router)

  await authStore.bootstrap()

  if (!authStore.isAuthenticated()) {
    return router.createUrlTree(['/login'])
  }
  if (authStore.profileCompleted()) {
    return router.createUrlTree(['/dashboard'])
  }
  return true
}
