import { defineConfig, devices } from '@playwright/test'

// E2E tests run against the real Angular dev server AND a real Spring Boot backend talking to a
// real Postgres database - the same "no mocking the thing you're actually trying to verify"
// principle the backend's Testcontainers-based integration tests already follow. CI provides
// Postgres as a service container and the datasource/JWT_SECRET env vars; locally, start your
// own Postgres and export the same variables before running `npm run e2e`.
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: process.env.CI ? [['html', { open: 'never' }], ['github']] : 'list',
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  webServer: [
    {
      command: 'npm run start',
      url: 'http://localhost:4200',
      reuseExistingServer: !process.env.CI,
      timeout: 120_000,
    },
    {
      command: 'npm run e2e:backend',
      url: 'http://localhost:8080/v3/api-docs',
      reuseExistingServer: !process.env.CI,
      timeout: 180_000,
    },
  ],
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
