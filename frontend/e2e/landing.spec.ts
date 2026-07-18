import { expect, test } from '@playwright/test'

test('landing page shows the value proposition and CTAs for anonymous visitors', async ({
  page,
}) => {
  await page.goto('/')

  await expect(
    page.getByRole('heading', { name: 'Behalte deine Ernährung im Griff' }),
  ).toBeVisible()
  await expect(
    page.getByRole('link', { name: 'Registrieren' }).first(),
  ).toBeVisible()
  await expect(page.getByRole('link', { name: 'Login' }).first()).toBeVisible()

  await page.getByRole('link', { name: 'Registrieren' }).first().click()
  await expect(page).toHaveURL(/\/register$/)
})
