import { inject } from '@angular/core'
import { CanActivateFn, Router } from '@angular/router'
import { AuthStore } from '../api/stores/auth.store'

// Forces logged in users with an incomplete profile back to /onboarding, no matter which
// protected page they try to open directly. Calls bootstrap() itself rather than relying on
// authGuard having already done so - Angular runs multiple canActivate guards on the same
// route in parallel, not sequentially, so this can run before authGuard's bootstrap settles.
export const profileCompleteGuard: CanActivateFn = async () => {
  const authStore = inject(AuthStore)
  const router = inject(Router)

  await authStore.bootstrap()

  if (!authStore.profileCompleted()) {
    return router.createUrlTree(['/onboarding'])
  }
  return true
}
