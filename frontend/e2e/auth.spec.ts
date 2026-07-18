import { expect, test } from '@playwright/test'
import {
  registerAndOnboard,
  submitRegisterForm,
  TEST_PASSWORD,
  uniqueEmail,
} from './helpers'

test.describe('registration and login', () => {
  test('registers, completes onboarding and lands on the dashboard with the right values', async ({
    page,
  }) => {
    const email = uniqueEmail('register')

    await registerAndOnboard(page, email, {
      height: '180',
      weight: '75',
      activityLevel: 'VERY_ACTIVE',
    })

    await expect(page.getByText(`Hallo ${email}`)).toBeVisible()
    await expect(page.getByText('23.1')).toBeVisible()
    await expect(page.getByText('Sehr aktiv')).toBeVisible()
  })

  test('rejects registration with an already used email', async ({ page }) => {
    const email = uniqueEmail('dup')

    // First registration succeeds and leaves the user logged in (on /onboarding), so cookies
    // must be cleared before the form can be reached a second time - publicOnlyGuard would
    // otherwise bounce an already-authenticated user straight back off /register.
    await submitRegisterForm(page, email)
    await expect(page).toHaveURL(/\/onboarding$/)

    await page.context().clearCookies()
    await submitRegisterForm(page, email)

    await expect(
      page.getByText('Diese E-Mail wird bereits verwendet.'),
    ).toBeVisible()
    await expect(page).toHaveURL(/\/register$/)
  })

  test('rejects login with a wrong password', async ({ page }) => {
    const email = uniqueEmail('wrongpw')
    await submitRegisterForm(page, email)
    await expect(page).toHaveURL(/\/onboarding$/)

    await page.context().clearCookies()
    await page.goto('/login')
    await page.getByPlaceholder('beispiel@mail.de').fill(email)
    await page.locator('input[type="password"]').fill('TotallyWrongPassword1')
    await page.getByRole('button', { name: 'Anmelden' }).click()

    await expect(
      page.getByText('E-Mail oder Passwort ist falsch.'),
    ).toBeVisible()
    await expect(page).toHaveURL(/\/login$/)
  })

  test('rejects login for an unknown email', async ({ page }) => {
    await page.goto('/login')
    await page.getByPlaceholder('beispiel@mail.de').fill(uniqueEmail('unknown'))
    await page.locator('input[type="password"]').fill(TEST_PASSWORD)
    await page.getByRole('button', { name: 'Anmelden' }).click()

    await expect(
      page.getByText('E-Mail oder Passwort ist falsch.'),
    ).toBeVisible()
  })

  test('logs in an existing user and returns straight to the dashboard', async ({
    page,
  }) => {
    const email = uniqueEmail('login')
    await registerAndOnboard(page, email, {
      height: '180',
      weight: '80',
      activityLevel: 'SEDENTARY',
    })

    await page.context().clearCookies()
    await page.goto('/login')
    await page.getByPlaceholder('beispiel@mail.de').fill(email)
    await page.locator('input[type="password"]').fill(TEST_PASSWORD)
    await page.getByRole('button', { name: 'Anmelden' }).click()

    await expect(page).toHaveURL(/\/dashboard$/)
    await expect(page.getByText(`Hallo ${email}`)).toBeVisible()
  })

  test('logs out via the header menu and can no longer reach the dashboard', async ({
    page,
  }) => {
    const email = uniqueEmail('logout')
    await registerAndOnboard(page, email, {
      height: '180',
      weight: '80',
      activityLevel: 'EXTRA_ACTIVE',
    })

    await page.getByRole('button', { name: /Benutzermenü/ }).click()
    await page.getByRole('button', { name: 'Abmelden' }).click()

    await expect(page).toHaveURL(/\/login$/)

    await page.goto('/dashboard')
    await expect(page).toHaveURL(/\/login$/)
  })
})
