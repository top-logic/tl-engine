/// <reference types="vitest/config" />
import { defineConfig } from 'vite';

// The test setup is a configuration of its own, so that the bundle build (vite.config.ts) stays untouched by it.
export default defineConfig({
  test: {
    include: ['react-src/**/*.test.{ts,tsx}'],
    // Chart.js draws on a canvas element.
    environment: 'jsdom',
    // A JUnit report next to the surefire reports of the module, so CI collects the JS test results together with the
    // Java ones.
    reporters: ['default', 'junit'],
    outputFile: { junit: 'target/surefire-reports/TEST-vitest.xml' },
  },
});
