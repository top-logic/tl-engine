/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

/** The module of tl-react-bridge, whose React the controls of this module render with. */
const BRIDGE_MODULE = path.resolve(__dirname, '../com.top_logic.layout.react');

// The test setup is a configuration of its own, so that the bundle build (vite.config.ts) stays
// untouched by it. In the bundle, tl-react-bridge is external and the libraries reach its React
// through the shims; here, the bridge is part of the test, and the libraries and the bridge resolve
// React to the one installed for the bridge.
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: [
      { find: 'tl-react-bridge', replacement: path.resolve(BRIDGE_MODULE, 'react-src/bridge-entry.ts') },
      { find: /^(react|react-dom)(\/.*)?$/, replacement: path.resolve(BRIDGE_MODULE, 'node_modules') + '/$1$2' },
    ],
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
