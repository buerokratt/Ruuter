// @ts-check
const { defineConfig, devices } = require('@playwright/test');

module.exports = defineConfig({
  testMatch: ['**/*.smoke.js', '**/*.spec.js'],
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
  },
  timeout: 30000,
  retries: 1,
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
