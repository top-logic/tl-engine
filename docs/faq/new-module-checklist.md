# FAQ: New Module Checklist

## Problem: Configuration not loaded / JS scripts not included / CSS not applied

When creating a new TopLogic module, several registration files are required beyond just `pom.xml`. Missing any of these causes silent failures where features simply don't work at runtime.

## Required files for a new module

### 1. `metaConf.txt` — Configuration file registration

**Path:** `src/main/webapp/WEB-INF/conf/metaConf.txt`

Lists the module's `.conf.config.xml` files that should be loaded at application startup. Without this file, the module's service configurations (JSFileCompiler, ModuleSystem, etc.) are **silently ignored**.

```
my-module.conf.config.xml
```

**Symptom if missing:** Services configured in `.conf.config.xml` don't take effect. For example, JS scripts registered via `JSFileCompiler` additional-files are not included in the HTML page, causing "Component not registered" errors for React controls.

### 2. `web-fragment.xml` — Servlet container registration

**Path:** `src/main/java/META-INF/web-fragment.xml`

Registers the module with the servlet container. Required for the module's web resources to be discoverable.

```xml
<web-fragment xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
    http://java.sun.com/xml/ns/javaee/web-fragment_3_0.xsd"
    version="3.0" metadata-complete="true">
  <name>my-module-name</name>
</web-fragment>
```

### 3. `theme.xml` — CSS stylesheet registration

**Path:** `src/main/webapp/WEB-INF/themes/core/theme.xml`

Registers CSS files with the TopLogic theme system. Without this, CSS files in `style/` are never loaded by the browser.

```xml
<?xml version="1.0" encoding="utf-8" ?>
<theme>
    <styles>
        <style name="/style/my-module.css"/>
    </styles>
</theme>
```

**Symptom if missing:** CSS classes have no effect, elements are unstyled.

### 4. Module registration in parent POM

**Path:** `tl-parent-engine/pom.xml`

Add `<module>../my.new.module</module>` in the `<modules>` section.

### 5. React/JS module: Shims for third-party React libraries

If your module bundles third-party npm libraries that `import from 'react'` (like `react-chartjs-2`, `react-select`, etc.), you must redirect these imports to `tl-react-bridge` via Vite `resolve.alias`. Otherwise, the library bundles its own React copy, causing "useState is null" / "useRef is null" errors.

```typescript
// vite.config.ts
resolve: {
  alias: {
    'react/jsx-runtime': path.resolve(__dirname, 'react-src/react-jsx-runtime-shim.ts'),
    'react-dom': path.resolve(__dirname, 'react-src/react-dom-shim.ts'),
    'react': path.resolve(__dirname, 'react-src/react-shim.ts'),
  },
},
```

The shim files re-export from `tl-react-bridge`:

```typescript
// react-shim.ts
import { React } from 'tl-react-bridge';
export default React;
export const { useState, useRef, useEffect, ... } = React;
```

**This is NOT needed** if your module only imports from `tl-react-bridge` directly (like `tl-demo` does). It's only needed when bundling third-party React libraries.

## Quick reference

| File | Purpose | Symptom if missing |
|------|---------|-------------------|
| `WEB-INF/conf/metaConf.txt` | Lists config files to load | Services/configs silently ignored |
| `META-INF/web-fragment.xml` | Servlet container registration | Module resources not found |
| `WEB-INF/themes/core/theme.xml` | CSS registration | Styles not applied |
| `tl-parent-engine/pom.xml` entry | Build inclusion | Module not built |
| React alias shims | Single React instance | "useState/useRef is null" errors |
