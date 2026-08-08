import { expect, Page } from '@playwright/test'

export const TEST_PASSWORD = 'SuperSecret123'

export function uniqueEmail(prefix: string): string {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100_000)}@example.com`
}

/**
 * Fills and submits the registration form. Does not wait for the resulting navigation -
 * callers assert on the outcome themselves (some register flows are expected to fail).
 */
export async function submitRegisterForm(
  page: Page,
  email: string,
  password = TEST_PASSWORD,
) {
  await page.goto('/register')
  await page.getByPlaceholder('beispiel@mail.de').fill(email)
  const passwordFields = page.locator('input[type="password"]')
  await passwordFields.nth(0).fill(password)
  await passwordFields.nth(1).fill(password)
  await page.getByRole('button', { name: 'Registrieren' }).click()
}

/** Registers a brand new user and completes onboarding, leaving the page on /dashboard. */
export async function registerAndOnboard(
  page: Page,
  email: string,
  profile: { height: string; weight: string; activityLevel: string },
) {
  await submitRegisterForm(page, email)
  await expect(page).toHaveURL(/\/onboarding$/)

  await page.getByPlaceholder('z.B. 175').fill(profile.height)
  await page.getByPlaceholder('z.B. 75').fill(profile.weight)
  await page.locator('select').selectOption(profile.activityLevel)
  await page.getByRole('button', { name: 'Fertigstellen' }).click()

  await expect(page).toHaveURL(/\/dashboard$/)
}
