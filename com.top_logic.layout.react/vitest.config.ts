/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The test setup is a configuration of its own, so that the two bundle builds
// (vite.config.bridge.ts, vite.config.controls.ts) stay untouched by it.
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { 'tl-react-bridge': new URL('./react-src/bridge-entry.ts', import.meta.url).pathname },
  },
  test: {
    include: ['react-src/**/*.test.{ts,tsx}'],
    // The bridge installs DOM observers when it is loaded.
    environment: 'jsdom',
    // A JUnit report next to the surefire reports of the module, so CI collects the JS test results
    // together with the Java ones.
    reporters: ['default', 'junit'],
    outputFile: { junit: 'target/surefire-reports/TEST-vitest.xml' },
  },
});
