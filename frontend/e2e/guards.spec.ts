import { expect, test } from '@playwright/test'
import { registerAndOnboard, submitRegisterForm, uniqueEmail } from './helpers'

test.describe('route guards', () => {
  test('redirects unauthenticated users away from every protected page', async ({
    page,
  }) => {
    for (const path of ['/dashboard', '/food', '/recipe', '/onboarding']) {
      await page.goto(path)
      await expect(page).toHaveURL(/\/login$/)
    }
  })

  test('keeps a user with an incomplete profile locked to /onboarding', async ({
    page,
  }) => {
    const email = uniqueEmail('guard-incomplete')
    await submitRegisterForm(page, email)
    await expect(page).toHaveURL(/\/onboarding$/)

    for (const path of ['/dashboard', '/food', '/recipe']) {
      await page.goto(path)
      await expect(page).toHaveURL(/\/onboarding$/)
    }
  })

  test('sends a fully onboarded user away from the public pages', async ({
    page,
  }) => {
    const email = uniqueEmail('guard-complete')
    await registerAndOnboard(page, email, {
      height: '170',
      weight: '65',
      activityLevel: 'LIGHTLY_ACTIVE',
    })

    for (const path of ['/', '/login', '/register', '/onboarding']) {
      await page.goto(path)
      await expect(page).toHaveURL(/\/dashboard$/)
    }
  })

  // Regression test: a routing config bug (missing pathMatch: 'full' on the empty-path landing
  // route) and a guard race condition (profileCompleteGuard reading state before authGuard's
  // bootstrap() had settled) previously bounced authenticated users straight back to /dashboard
  // whenever they navigated to /food or /recipe.
  test('food and recipe pages actually load for an authenticated, onboarded user', async ({
    page,
  }) => {
    const email = uniqueEmail('guard-food')
    await registerAndOnboard(page, email, {
      height: '175',
      weight: '70',
      activityLevel: 'MODERATELY_ACTIVE',
    })

    await page.goto('/food')
    await expect(page).toHaveURL(/\/food$/)
    await expect(
      page.getByRole('button', { name: 'Lebensmittel hinzufügen' }),
    ).toBeVisible()

    await page.goto('/recipe')
    await expect(page).toHaveURL(/\/recipe$/)
  })
})
