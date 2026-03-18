# FAQ: New React Control Module

## When to use

When you need React controls in a separate module (not in `com.top_logic.layout.react` itself). Typical reasons:
- Third-party npm library integration (Chart.js, CodeMirror, etc.)
- Domain-specific controls that don't belong in the core React module

## Required files

All files from the [general module checklist](new-module-checklist.md) plus the following.

### 1. `package.json`

```json
{
  "name": "tl-my-module",
  "version": "7.11.0",
  "private": true,
  "scripts": {
    "build": "vite build"
  },
  "devDependencies": {
    "@types/react": "^19.0.0",
    "@vitejs/plugin-react": "^4.3.0",
    "typescript": "^5.7.0",
    "vite": "^6.0.0"
  }
}
```

Add third-party React libraries (e.g. `react-chartjs-2`) under `dependencies`, **not** `devDependencies`.

Do **not** add `react` or `react-dom` as dependencies — they come from `tl-react-bridge`.

### 2. `tsconfig.json`

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "jsx": "react-jsx",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "outDir": "dist",
    "paths": {
      "tl-react-bridge": ["../com.top_logic.layout.react/react-src/bridge-entry.ts"]
    }
  },
  "include": ["react-src"]
}
```

The `paths` mapping enables TypeScript to resolve `tl-react-bridge` types during development.

### 3. `vite.config.ts`

#### Simple case (no third-party React libraries)

If your controls only import from `tl-react-bridge` (like `tl-demo`):

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  build: {
    lib: {
      entry: 'react-src/controls-entry.ts',
      fileName: () => 'tl-my-module.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge'],
    },
  },
});
```

#### With third-party React libraries

If your module uses npm packages that `import from 'react'` internally (e.g. `react-chartjs-2`, `react-select`), you **must** add `resolve.alias` to redirect these imports to `tl-react-bridge`. Without this, the library bundles its own React copy, causing **"useState is null"** errors at runtime.

```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react({ jsxRuntime: 'classic' })],
  define: {
    'process.env.NODE_ENV': JSON.stringify('production'),
  },
  resolve: {
    alias: {
      // Order matters: more specific paths first.
      'react/jsx-runtime': path.resolve(__dirname, 'react-src/react-jsx-runtime-shim.ts'),
      'react-dom': path.resolve(__dirname, 'react-src/react-dom-shim.ts'),
      'react': path.resolve(__dirname, 'react-src/react-shim.ts'),
    },
  },
  build: {
    lib: {
      entry: 'react-src/controls-entry.ts',
      fileName: () => 'tl-my-module.js',
      formats: ['es'],
    },
    outDir: 'src/main/webapp/script',
    emptyOutDir: false,
    rollupOptions: {
      external: ['tl-react-bridge'],
    },
  },
});
```

The three shim files:

**`react-src/react-shim.ts`**
```typescript
import { React } from 'tl-react-bridge';
export default React;
export const {
  useState, useRef, useEffect, useCallback, useMemo,
  forwardRef, createRef, createElement, createContext,
  useContext, useReducer, useImperativeHandle, useLayoutEffect,
  memo, Fragment, Children, isValidElement, cloneElement
} = React;
```

**`react-src/react-dom-shim.ts`**
```typescript
import { ReactDOM } from 'tl-react-bridge';
export default ReactDOM;
```

**`react-src/react-jsx-runtime-shim.ts`**
```typescript
import { React } from 'tl-react-bridge';
export const jsx = React.createElement;
export const jsxs = React.createElement;
export const Fragment = React.Fragment;
```

### 4. Entry file

```typescript
// react-src/controls-entry.ts
//
// IMPORTANT: All components MUST import React from 'tl-react-bridge' (not 'react').

import { register } from 'tl-react-bridge';
import MyControl from './controls/MyControl';

register('MyControl', MyControl);
```

### 5. `pom.xml` — frontend-maven-plugin

Add to `<build><plugins>`:

```xml
<plugin>
  <groupId>com.github.eirslett</groupId>
  <artifactId>frontend-maven-plugin</artifactId>
  <version>1.15.4</version>
  <executions>
    <execution>
      <id>install-node</id>
      <goals><goal>install-node-and-npm</goal></goals>
      <configuration>
        <nodeVersion>v20.10.0</nodeVersion>
      </configuration>
    </execution>
    <execution>
      <id>npm-install</id>
      <goals><goal>npm</goal></goals>
      <configuration><arguments>install</arguments></configuration>
    </execution>
    <execution>
      <id>npm-build</id>
      <phase>generate-resources</phase>
      <goals><goal>npm</goal></goals>
      <configuration><arguments>run build</arguments></configuration>
    </execution>
  </executions>
</plugin>
```

### 6. `metaConf.txt` + JSFileCompiler config

**`src/main/webapp/WEB-INF/conf/metaConf.txt`:**
```
tl-my-module.conf.config.xml
```

**`src/main/webapp/WEB-INF/conf/tl-my-module.conf.config.xml`:**
```xml
<?xml version="1.0" encoding="utf-8" ?>
<application xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <services>
    <config service-class="com.top_logic.gui.JSFileCompiler">
      <instance>
        <additional-files>
          <file resource="tl-my-module.js" type="module" />
        </additional-files>
      </instance>
    </config>
  </services>
</application>
```

### 7. Control component pattern

```typescript
import { React, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const MyControl: React.FC<TLCellProps> = ({ controlId, state }) => {
  const sendCommand = useTLCommand();
  // Use React.useState, React.useRef, etc. (from tl-react-bridge)
  return <div id={controlId}>...</div>;
};

export default MyControl;
```

### 8. Java UIElement + ReactControl

See `ChartElement.java` and `ReactChartJsControl.java` in `com.top_logic.layout.react.chartjs` for the full pattern, or `DemoCounterElement.java` and `DemoCounterControl` in `com.top_logic.demo` for a minimal example.

## Build

Always from project root:
```bash
mvn install -DskipTests=true -pl my.new.module
```

Never `cd` into the module. Never run `npx vite build` directly.

## Common errors

| Error | Cause | Fix |
|-------|-------|-----|
| "Component not registered: X" | `metaConf.txt` missing or JS not in `additional-files` | Add `metaConf.txt`, check `conf.config.xml` |
| "useState is null" / "useRef is null" | Duplicate React instance from third-party lib | Add `resolve.alias` shims in `vite.config.ts` |
| Script not in HTML page | `metaConf.txt` missing | Create `metaConf.txt` listing the `.conf.config.xml` |
| CSS not applied | `theme.xml` missing | Create `WEB-INF/themes/core/theme.xml` |
| Build fails with "Cannot find module 'tl-react-bridge'" | `tsconfig.json` paths missing | Add `paths` mapping to `tsconfig.json` |
