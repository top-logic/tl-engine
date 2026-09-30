# React WYSIWYG Editor (TipTap) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a TipTap-based WYSIWYG HTML editor as a React form field control for `tl.model.wysiwyg:Html` attributes.

**Architecture:** New Maven module `com.top_logic.layout.react.wysiwyg` following the `com.top_logic.layout.react.chartjs` pattern — separate Vite build with React shims, Java control extending `ReactFormFieldControl`, type mapping via `ReactComponentRegistry`. `DataProvider` interface is extended with a `key` parameter for multi-image serving.

**Tech Stack:** TipTap 2.x (ProseMirror), TypeScript, Vite, Java 17, TopLogic React Bridge

**Spec:** `docs/superpowers/specs/2026-03-26-react-wysiwyg-editor-design.md`

---

## File Structure

### New files (com.top_logic.layout.react.wysiwyg)

| File | Responsibility |
|------|---------------|
| `pom.xml` | Maven module with frontend-maven-plugin |
| `package.json` | TipTap npm dependencies |
| `tsconfig.json` | TypeScript config |
| `vite.config.ts` | Vite build with React shims, externalize tl-react-bridge |
| `react-src/wysiwyg-entry.ts` | Register TLWysiwygEditor component |
| `react-src/react-shim.ts` | Re-export React from tl-react-bridge |
| `react-src/react-dom-shim.ts` | Re-export ReactDOM from tl-react-bridge |
| `react-src/react-jsx-runtime-shim.ts` | Re-export JSX runtime from tl-react-bridge |
| `react-src/controls/TLWysiwygEditor.tsx` | Main editor component |
| `react-src/controls/WysiwygToolbar.tsx` | Toolbar component |
| `react-src/controls/TLWysiwygEditor.css` | Editor styles |
| `src/main/java/META-INF/web-fragment.xml` | Servlet fragment |
| `src/main/java/com/top_logic/layout/react/wysiwyg/ReactWysiwygControl.java` | Java control |
| `src/main/java/com/top_logic/layout/react/wysiwyg/I18NConstants.java` | I18N keys |
| `src/main/webapp/WEB-INF/conf/metaConf.txt` | Config file listing |
| `src/main/webapp/WEB-INF/conf/tl-layout-react-wysiwyg.conf.config.xml` | Module config |

### Modified files

| File | Change |
|------|--------|
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DataProvider.java` | Add `key` parameter to `getDownloadData` |
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java:110-146` | Pass `key` parameter to `getDownloadData` |
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/audio/ReactAudioPlayerControl.java` | Update `getDownloadData` signature |
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/photo/ReactPhotoViewerControl.java` | Update `getDownloadData` signature |
| `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/download/ReactDownloadControl.java` | Update `getDownloadData` signature |
| `tl-parent-engine/pom.xml:88` | Add module entry |
| `com.top_logic.demo/pom.xml` | Add dependency |
| `com.top_logic.demo/src/main/webapp/WEB-INF/model/DemoTypes.model.xml:253` | Add Html attribute to DemoTypes.A |

---

## Task 1: Extend DataProvider with key parameter

This task modifies the existing `com.top_logic.layout.react` module. It must be done first because the new wysiwyg module depends on it.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DataProvider.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/servlet/ReactServlet.java:110-146`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/audio/ReactAudioPlayerControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/photo/ReactPhotoViewerControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/download/ReactDownloadControl.java`

- [ ] **Step 1: Change DataProvider interface**

Replace the contents of `DataProvider.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.servlet.ReactServlet;

/**
 * Interface for React controls that serve binary data to the client.
 *
 * <p>
 * Controls implementing this interface can provide binary data (e.g. audio, images) for download by
 * the React client via the {@code /react-api/data} endpoint. The {@link ReactServlet} dispatches
 * incoming GET requests to the appropriate control based on the {@code controlId} parameter.
 * </p>
 *
 * <p>
 * The optional {@code key} parameter allows controls that manage multiple data items (e.g. a
 * WYSIWYG editor with embedded images) to serve specific items by key.
 * </p>
 */
public interface DataProvider {

	/**
	 * Returns the binary data for the given key, or {@code null} if no data is available.
	 *
	 * @param key
	 *        Identifies the requested data item. May be {@code null} for controls that serve a
	 *        single item.
	 * @return The binary data, or {@code null}.
	 */
	BinaryData getDownloadData(String key);

}
```

- [ ] **Step 2: Update ReactServlet to pass key parameter**

In `ReactServlet.java`, change line 131 from:
```java
		BinaryData data = ((DataProvider) control).getDownloadData();
```
to:
```java
		String key = request.getParameter("key");
		BinaryData data = ((DataProvider) control).getDownloadData(key);
```

- [ ] **Step 3: Update ReactAudioPlayerControl**

Change `getDownloadData()` to `getDownloadData(String key)` (ignore the key parameter):
```java
	@Override
	public BinaryData getDownloadData(String key) {
		return _model.getData();
	}
```

- [ ] **Step 4: Update ReactPhotoViewerControl**

Same change:
```java
	@Override
	public BinaryData getDownloadData(String key) {
		return _model.getData();
	}
```

- [ ] **Step 5: Update ReactDownloadControl**

Same change:
```java
	@Override
	public BinaryData getDownloadData(String key) {
		return _model.getData();
	}
```

- [ ] **Step 6: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```
Ticket #29108: Add key parameter to DataProvider for multi-item data serving.
```

---

## Task 2: Create module skeleton (Maven + npm + Vite)

**Files:**
- Create: `com.top_logic.layout.react.wysiwyg/pom.xml`
- Create: `com.top_logic.layout.react.wysiwyg/package.json`
- Create: `com.top_logic.layout.react.wysiwyg/tsconfig.json`
- Create: `com.top_logic.layout.react.wysiwyg/vite.config.ts`
- Create: `com.top_logic.layout.react.wysiwyg/react-src/react-shim.ts`
- Create: `com.top_logic.layout.react.wysiwyg/react-src/react-dom-shim.ts`
- Create: `com.top_logic.layout.react.wysiwyg/react-src/react-jsx-runtime-shim.ts`
- Create: `com.top_logic.layout.react.wysiwyg/react-src/wysiwyg-entry.ts` (placeholder)
- Create: `com.top_logic.layout.react.wysiwyg/src/main/java/META-INF/web-fragment.xml`
- Create: `com.top_logic.layout.react.wysiwyg/src/main/webapp/WEB-INF/conf/metaConf.txt`
- Create: `com.top_logic.layout.react.wysiwyg/src/main/webapp/WEB-INF/conf/tl-layout-react-wysiwyg.conf.config.xml`
- Modify: `tl-parent-engine/pom.xml:88`

- [ ] **Step 1: Create pom.xml**

Create `com.top_logic.layout.react.wysiwyg/pom.xml`:

```xml
<?xml version="1.0"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <relativePath>../tl-parent-core/internal</relativePath>
    <groupId>com.top-logic</groupId>
    <artifactId>tl-parent-core-internal</artifactId>
    <version>7.11.0-SNAPSHOT</version>
  </parent>

  <artifactId>tl-layout-react-wysiwyg</artifactId>
  <name>${project.artifactId}</name>
  <description>React-based TipTap WYSIWYG editor for TopLogic Html attributes.</description>
  <url>https://github.com/top-logic/tl-engine/</url>

  <dependencies>
    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-layout-react</artifactId>
    </dependency>

    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-layout-wysiwyg</artifactId>
    </dependency>

    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-basic</artifactId>
      <type>test-jar</type>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>com.github.eirslett</groupId>
        <artifactId>frontend-maven-plugin</artifactId>
        <version>1.15.4</version>

        <executions>
          <execution>
            <id>install-node</id>
            <goals>
              <goal>install-node-and-npm</goal>
            </goals>
            <configuration>
              <nodeVersion>v20.10.0</nodeVersion>
            </configuration>
          </execution>

          <execution>
            <id>npm-install</id>
            <goals>
              <goal>npm</goal>
            </goals>
            <configuration>
              <arguments>install</arguments>
            </configuration>
          </execution>

          <execution>
            <id>npm-build</id>
            <phase>generate-resources</phase>
            <goals>
              <goal>npm</goal>
            </goals>
            <configuration>
              <arguments>run build</arguments>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 2: Create package.json**

Create `com.top_logic.layout.react.wysiwyg/package.json`:

```json
{
  "name": "tl-react-wysiwyg",
  "version": "7.11.0",
  "private": true,
  "scripts": {
    "build": "vite build"
  },
  "dependencies": {
    "@tiptap/react": "^2.10.0",
    "@tiptap/starter-kit": "^2.10.0",
    "@tiptap/extension-underline": "^2.10.0",
    "@tiptap/extension-link": "^2.10.0",
    "@tiptap/extension-image": "^2.10.0",
    "@tiptap/extension-table": "^2.10.0",
    "@tiptap/extension-table-row": "^2.10.0",
    "@tiptap/extension-table-cell": "^2.10.0",
    "@tiptap/extension-table-header": "^2.10.0",
    "@tiptap/extension-color": "^2.10.0",
    "@tiptap/extension-text-style": "^2.10.0"
  },
  "devDependencies": {
    "@types/react": "^19.0.0",
    "@vitejs/plugin-react": "^4.3.0",
    "typescript": "^5.7.0",
    "vite": "^6.0.0"
  }
}
```

- [ ] **Step 3: Create tsconfig.json**

Create `com.top_logic.layout.react.wysiwyg/tsconfig.json`:

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

- [ ] **Step 4: Create vite.config.ts**

Create `com.top_logic.layout.react.wysiwyg/vite.config.ts`:

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
      'react/jsx-runtime': path.resolve(__dirname, 'react-src/react-jsx-runtime-shim.ts'),
      'react-dom': path.resolve(__dirname, 'react-src/react-dom-shim.ts'),
      'react': path.resolve(__dirname, 'react-src/react-shim.ts'),
    },
  },
  build: {
    lib: {
      entry: 'react-src/wysiwyg-entry.ts',
      fileName: () => 'tl-react-wysiwyg.js',
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

- [ ] **Step 5: Create React shims**

Copy from `com.top_logic.layout.react.chartjs/react-src/`. Create three files:

`com.top_logic.layout.react.wysiwyg/react-src/react-shim.ts`:
```typescript
import { React, ReactDOM } from 'tl-react-bridge';
export default React;
export { ReactDOM };
export const {
  useState, useRef, useEffect, useCallback, useMemo,
  forwardRef, createRef, createElement, createContext,
  useContext, useReducer, useImperativeHandle, useLayoutEffect,
  memo, Fragment, Children, isValidElement, cloneElement
} = React;
```

`com.top_logic.layout.react.wysiwyg/react-src/react-dom-shim.ts`:
```typescript
import { ReactDOM } from 'tl-react-bridge';
export default ReactDOM;
```

`com.top_logic.layout.react.wysiwyg/react-src/react-jsx-runtime-shim.ts`:
```typescript
import { React } from 'tl-react-bridge';
export const jsx = React.createElement;
export const jsxs = React.createElement;
export const Fragment = React.Fragment;
```

- [ ] **Step 6: Create placeholder entry point**

Create `com.top_logic.layout.react.wysiwyg/react-src/wysiwyg-entry.ts`:

```typescript
// TipTap WYSIWYG editor registration for the tl-react-wysiwyg bundle.
//
// IMPORTANT: All components MUST import React from 'tl-react-bridge' (not 'react').

import { register } from 'tl-react-bridge';
// Component will be added in Task 4.
```

- [ ] **Step 7: Create Java META-INF files**

Create `com.top_logic.layout.react.wysiwyg/src/main/java/META-INF/web-fragment.xml`:

```xml
<web-fragment xmlns="http://java.sun.com/xml/ns/javaee"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
    http://java.sun.com/xml/ns/javaee/web-fragment_3_0.xsd"
    version="3.0" metadata-complete="true">
  <name>tl-layout-react-wysiwyg</name>
</web-fragment>
```

- [ ] **Step 8: Create webapp config files**

Create `com.top_logic.layout.react.wysiwyg/src/main/webapp/WEB-INF/conf/metaConf.txt`:
```
tl-layout-react-wysiwyg.conf.config.xml
```

Create `com.top_logic.layout.react.wysiwyg/src/main/webapp/WEB-INF/conf/tl-layout-react-wysiwyg.conf.config.xml`:
```xml
<?xml version="1.0" encoding="utf-8" ?>

<application xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <services>
    <config service-class="com.top_logic.layout.react.ReactComponentRegistry">
      <instance>
        <type-mappings>
          <type-mapping type="tl.model.wysiwyg:Html" module="TLWysiwygEditor"/>
        </type-mappings>
      </instance>
    </config>

    <config service-class="com.top_logic.gui.JSFileCompiler">
      <instance>
        <additional-files>
          <file resource="tl-react-wysiwyg.js" type="module" />
        </additional-files>
      </instance>
    </config>
  </services>
</application>
```

- [ ] **Step 9: Register module in tl-parent-engine**

In `tl-parent-engine/pom.xml`, after line 88 (`../com.top_logic.layout.react.chartjs`), add:
```xml
		<module>../com.top_logic.layout.react.wysiwyg</module>
```

- [ ] **Step 10: Build skeleton to verify**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react.wysiwyg`
Expected: BUILD SUCCESS (npm install + vite build runs, produces `tl-react-wysiwyg.js`)

- [ ] **Step 11: Commit**

```
Ticket #29108: Add module skeleton for com.top_logic.layout.react.wysiwyg.
```

---

## Task 3: Java control — ReactWysiwygControl

**Files:**
- Create: `com.top_logic.layout.react.wysiwyg/src/main/java/com/top_logic/layout/react/wysiwyg/ReactWysiwygControl.java`
- Create: `com.top_logic.layout.react.wysiwyg/src/main/java/com/top_logic/layout/react/wysiwyg/I18NConstants.java`

- [ ] **Step 1: Create I18NConstants**

Create `com.top_logic.layout.react.wysiwyg/src/main/java/com/top_logic/layout/react/wysiwyg/I18NConstants.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for the React WYSIWYG module.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Image upload failed.
	 */
	public static ResKey ERROR_IMAGE_UPLOAD_FAILED;

	static {
		initConstants(I18NConstants.class);
	}
}
```

- [ ] **Step 2: Create ReactWysiwygControl**

Create `com.top_logic.layout.react.wysiwyg/src/main/java/com/top_logic/layout/react/wysiwyg/ReactWysiwygControl.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.Part;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * React WYSIWYG editor control for {@code tl.model.wysiwyg:Html} attributes.
 *
 * <p>
 * Renders the {@code TLWysiwygEditor} React component backed by TipTap. Converts between
 * {@link StructuredText} (server model) and HTML strings (client). Serves embedded images via
 * {@link DataProvider} and accepts image uploads via {@link UploadHandler}.
 * </p>
 *
 * <p>
 * Image URLs are rewritten when sending HTML to the client: bare filenames in
 * {@code <img src="file.png">} become full download URLs
 * {@code <img src="/react-api/data?controlId=...&key=file.png">}. The reverse transformation is
 * applied when receiving HTML back from the client.
 * </p>
 */
public class ReactWysiwygControl extends ReactFormFieldControl implements UploadHandler, DataProvider {

	private static final String TOOLBAR = "toolbar";

	private static final String IMAGE_URL = "imageUrl";

	private static final String DATA_URL_PREFIX = "react-api/data?controlId=";

	private static final String KEY_PARAM = "&key=";

	private static final List<String> DEFAULT_TOOLBAR = Arrays.asList(
		"bold", "italic", "underline", "strike",
		"|",
		"heading",
		"|",
		"bulletList", "orderedList", "blockquote",
		"|",
		"link", "image", "table", "codeBlock",
		"|",
		"color",
		"|",
		"undo", "redo"
	);

	private final String _imageUrlPrefix;

	private StructuredText _shadowCopy;

	/**
	 * Creates a new {@link ReactWysiwygControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model holding the {@link StructuredText} value.
	 */
	public ReactWysiwygControl(ReactContext context, FieldModel model) {
		super(context, model, "TLWysiwygEditor");

		_imageUrlPrefix = DATA_URL_PREFIX + getId() + KEY_PARAM;

		initShadowCopy();
		putState(VALUE, rewriteImageUrls(extractHtml(_shadowCopy)));
		putState(TOOLBAR, DEFAULT_TOOLBAR);
	}

	private void initShadowCopy() {
		StructuredText value = (StructuredText) getFieldModel().getValue();
		if (value != null) {
			_shadowCopy = value.copy();
		} else {
			_shadowCopy = new StructuredText();
		}
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		if (newValue instanceof StructuredText) {
			_shadowCopy = ((StructuredText) newValue).copy();
			putState(VALUE, rewriteImageUrls(extractHtml(_shadowCopy)));
		} else {
			_shadowCopy = new StructuredText();
			putState(VALUE, "");
		}
	}

	@Override
	protected Object parseClientValue(Object rawValue) {
		String html = rawValue != null ? rawValue.toString() : "";
		String cleanHtml = stripImageUrls(html);
		_shadowCopy.setSourceCode(cleanHtml);
		removeUnusedImages(_shadowCopy);
		return _shadowCopy.copy();
	}

	@Override
	public BinaryData getDownloadData(String key) {
		if (_shadowCopy == null || key == null) {
			return null;
		}
		return _shadowCopy.getImages().get(key);
	}

	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		for (Part part : parts) {
			try {
				String fileName = uniqueImageKey(part.getSubmittedFileName());
				BinaryData imageData = BinaryDataFactory.createUploadData(part);
				_shadowCopy.addImage(fileName, imageData);
				putState(IMAGE_URL, fileName);
			} catch (IOException ex) {
				throw new TopLogicException(I18NConstants.ERROR_IMAGE_UPLOAD_FAILED, ex);
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Rewrites bare image filenames in {@code <img src>} to full download URLs for the client.
	 */
	private String rewriteImageUrls(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		Document doc = Jsoup.parse(html);
		Elements imgs = doc.select("img[src]");
		for (Element img : imgs) {
			String src = img.attr("src");
			if (!src.contains(DATA_URL_PREFIX) && _shadowCopy.getImages().containsKey(src)) {
				img.attr("src", _imageUrlPrefix + URLEncoder.encode(src, StandardCharsets.UTF_8));
			}
		}
		return doc.body().html();
	}

	/**
	 * Strips download URLs from {@code <img src>} back to bare image keys for storage.
	 */
	private String stripImageUrls(String html) {
		if (html == null || html.isEmpty()) {
			return html;
		}
		Document doc = Jsoup.parse(html);
		Elements imgs = doc.select("img[src]");
		for (Element img : imgs) {
			String src = img.attr("src");
			int keyIndex = src.indexOf(KEY_PARAM);
			if (keyIndex >= 0) {
				String key = src.substring(keyIndex + KEY_PARAM.length());
				int ampIndex = key.indexOf('&');
				if (ampIndex >= 0) {
					key = key.substring(0, ampIndex);
				}
				img.attr("src", URLDecoder.decode(key, StandardCharsets.UTF_8));
			}
		}
		return doc.body().html();
	}

	private String extractHtml(StructuredText text) {
		if (text == null) {
			return "";
		}
		return text.getSourceCode();
	}

	private String uniqueImageKey(String name) {
		if (name == null) {
			name = "image";
		}
		Map<String, BinaryData> images = _shadowCopy.getImages();
		if (!images.containsKey(name)) {
			return name;
		}
		int dot = name.lastIndexOf('.');
		String base = dot > 0 ? name.substring(0, dot) : name;
		String ext = dot > 0 ? name.substring(dot) : "";
		int counter = 1;
		String candidate;
		do {
			candidate = base + "_" + counter + ext;
			counter++;
		} while (images.containsKey(candidate));
		return candidate;
	}

	private static void removeUnusedImages(StructuredText text) {
		Document doc = Jsoup.parse(text.getSourceCode());
		Elements imgs = doc.select("img[src]");
		Set<String> usedKeys = new HashSet<>();
		for (Element img : imgs) {
			usedKeys.add(img.attr("src"));
		}
		text.getImages().keySet().retainAll(usedKeys);
	}
}
```

- [ ] **Step 3: Build to verify compilation**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react.wysiwyg`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
Ticket #29108: Add ReactWysiwygControl with StructuredText conversion and image handling.
```

---

## Task 4: React component — TLWysiwygEditor

**Files:**
- Create: `com.top_logic.layout.react.wysiwyg/react-src/controls/TLWysiwygEditor.tsx`
- Create: `com.top_logic.layout.react.wysiwyg/react-src/controls/WysiwygToolbar.tsx`
- Create: `com.top_logic.layout.react.wysiwyg/react-src/controls/TLWysiwygEditor.css`
- Modify: `com.top_logic.layout.react.wysiwyg/react-src/wysiwyg-entry.ts`

- [ ] **Step 1: Create TLWysiwygEditor.css**

Create `com.top_logic.layout.react.wysiwyg/react-src/controls/TLWysiwygEditor.css`:

```css
.tlWysiwygEditor {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--tl-border-color, #ccc);
  border-radius: 4px;
  overflow: hidden;
}

.tlWysiwygEditor--error {
  border-color: var(--tl-error-color, #d32f2f);
}

.tlWysiwygEditor--immutable {
  border: none;
}

.tlWysiwygEditor__content {
  padding: 8px 12px;
  min-height: 120px;
  outline: none;
  overflow-y: auto;
}

.tlWysiwygEditor__content .ProseMirror {
  outline: none;
  min-height: 120px;
}

.tlWysiwygEditor__content .ProseMirror p {
  margin: 0.4em 0;
}

.tlWysiwygEditor__content .ProseMirror table {
  border-collapse: collapse;
  width: 100%;
}

.tlWysiwygEditor__content .ProseMirror td,
.tlWysiwygEditor__content .ProseMirror th {
  border: 1px solid var(--tl-border-color, #ccc);
  padding: 4px 8px;
  min-width: 60px;
}

.tlWysiwygEditor__content .ProseMirror th {
  background: var(--tl-header-bg, #f5f5f5);
  font-weight: bold;
}

.tlWysiwygEditor__content .ProseMirror img {
  max-width: 100%;
  height: auto;
}

.tlWysiwygEditor__content .ProseMirror code {
  background: var(--tl-code-bg, #f0f0f0);
  padding: 2px 4px;
  border-radius: 2px;
  font-family: monospace;
}

.tlWysiwygEditor__content .ProseMirror pre {
  background: var(--tl-code-bg, #f0f0f0);
  padding: 8px 12px;
  border-radius: 4px;
  overflow-x: auto;
}

.tlWysiwygEditor__content .ProseMirror blockquote {
  border-left: 3px solid var(--tl-border-color, #ccc);
  padding-left: 12px;
  margin-left: 0;
  color: var(--tl-text-secondary, #666);
}

.tlWysiwygEditor__immutableContent {
  padding: 8px 12px;
}

/* Toolbar */
.tlWysiwygToolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 2px;
  padding: 4px 8px;
  border-bottom: 1px solid var(--tl-border-color, #ccc);
  background: var(--tl-toolbar-bg, #fafafa);
}

.tlWysiwygToolbar__button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 28px;
  padding: 2px 6px;
  border: 1px solid transparent;
  border-radius: 3px;
  background: none;
  cursor: pointer;
  font-size: 14px;
  font-family: inherit;
  color: var(--tl-text-color, #333);
}

.tlWysiwygToolbar__button:hover {
  background: var(--tl-hover-bg, #e8e8e8);
}

.tlWysiwygToolbar__button--active {
  background: var(--tl-selected-bg, #d0d0d0);
  border-color: var(--tl-border-color, #ccc);
}

.tlWysiwygToolbar__separator {
  width: 1px;
  height: 20px;
  margin: 0 4px;
  background: var(--tl-border-color, #ccc);
}
```

- [ ] **Step 2: Create WysiwygToolbar**

Create `com.top_logic.layout.react.wysiwyg/react-src/controls/WysiwygToolbar.tsx`:

```typescript
import { React } from 'tl-react-bridge';
import type { Editor } from '@tiptap/react';

interface ToolbarProps {
  editor: Editor | null;
  items: string[];
  onImageUpload: () => void;
}

interface ToolbarButtonDef {
  label: string;
  action: (editor: Editor) => void;
  isActive?: (editor: Editor) => boolean;
}

const BUTTON_DEFS: Record<string, ToolbarButtonDef> = {
  bold: {
    label: 'B',
    action: (e) => e.chain().focus().toggleBold().run(),
    isActive: (e) => e.isActive('bold'),
  },
  italic: {
    label: 'I',
    action: (e) => e.chain().focus().toggleItalic().run(),
    isActive: (e) => e.isActive('italic'),
  },
  underline: {
    label: 'U',
    action: (e) => e.chain().focus().toggleUnderline().run(),
    isActive: (e) => e.isActive('underline'),
  },
  strike: {
    label: 'S',
    action: (e) => e.chain().focus().toggleStrike().run(),
    isActive: (e) => e.isActive('strike'),
  },
  heading: {
    label: 'H',
    action: (e) => e.chain().focus().toggleHeading({ level: 2 }).run(),
    isActive: (e) => e.isActive('heading'),
  },
  bulletList: {
    label: '\u2022',
    action: (e) => e.chain().focus().toggleBulletList().run(),
    isActive: (e) => e.isActive('bulletList'),
  },
  orderedList: {
    label: '1.',
    action: (e) => e.chain().focus().toggleOrderedList().run(),
    isActive: (e) => e.isActive('orderedList'),
  },
  blockquote: {
    label: '\u201C',
    action: (e) => e.chain().focus().toggleBlockquote().run(),
    isActive: (e) => e.isActive('blockquote'),
  },
  link: {
    label: '\uD83D\uDD17',
    action: (e) => {
      const url = window.prompt('URL:');
      if (url) {
        e.chain().focus().setLink({ href: url }).run();
      }
    },
    isActive: (e) => e.isActive('link'),
  },
  image: {
    label: '\uD83D\uDDBC',
    action: () => { /* handled via onImageUpload */ },
  },
  table: {
    label: '\u2637',
    action: (e) => e.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run(),
  },
  codeBlock: {
    label: '</>',
    action: (e) => e.chain().focus().toggleCodeBlock().run(),
    isActive: (e) => e.isActive('codeBlock'),
  },
  color: {
    label: 'A',
    action: (e) => {
      const color = window.prompt('Color (hex):', '#000000');
      if (color) {
        e.chain().focus().setColor(color).run();
      }
    },
  },
  undo: {
    label: '\u21B6',
    action: (e) => e.chain().focus().undo().run(),
  },
  redo: {
    label: '\u21B7',
    action: (e) => e.chain().focus().redo().run(),
  },
};

const WysiwygToolbar: React.FC<ToolbarProps> = ({ editor, items, onImageUpload }) => {
  if (!editor) return null;

  return (
    <div className="tlWysiwygToolbar">
      {items.map((item, index) => {
        if (item === '|') {
          return <span key={index} className="tlWysiwygToolbar__separator" />;
        }
        const def = BUTTON_DEFS[item];
        if (!def) return null;
        const active = def.isActive ? def.isActive(editor) : false;
        const onClick = item === 'image' ? onImageUpload : () => def.action(editor);
        return (
          <button
            key={item}
            type="button"
            className={'tlWysiwygToolbar__button' + (active ? ' tlWysiwygToolbar__button--active' : '')}
            onMouseDown={(e) => { e.preventDefault(); onClick(); }}
            title={item}
          >
            {def.label}
          </button>
        );
      })}
    </div>
  );
};

export default WysiwygToolbar;
```

- [ ] **Step 3: Create TLWysiwygEditor**

Create `com.top_logic.layout.react.wysiwyg/react-src/controls/TLWysiwygEditor.tsx`:

```typescript
import { React, useTLState, useTLCommand, useTLUpload, useTLDataUrl } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import Link from '@tiptap/extension-link';
import Image from '@tiptap/extension-image';
import Table from '@tiptap/extension-table';
import TableRow from '@tiptap/extension-table-row';
import TableCell from '@tiptap/extension-table-cell';
import TableHeader from '@tiptap/extension-table-header';
import Color from '@tiptap/extension-color';
import TextStyle from '@tiptap/extension-text-style';
import WysiwygToolbar from './WysiwygToolbar';
import './TLWysiwygEditor.css';

const DEFAULT_TOOLBAR = [
  'bold', 'italic', 'underline', 'strike', '|',
  'heading', '|',
  'bulletList', 'orderedList', 'blockquote', '|',
  'link', 'image', 'table', 'codeBlock', '|',
  'color', '|',
  'undo', 'redo',
];

const TLWysiwygEditor: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const uploadFile = useTLUpload();
  const dataUrl = useTLDataUrl();

  const value: string = (state.value as string) || '';
  const editable: boolean = state.editable !== false;
  const toolbar: string[] = (state.toolbar as string[]) || DEFAULT_TOOLBAR;
  const hasError: boolean = !!state.hasError;
  const imageUrl: string | null = (state.imageUrl as string) || null;

  const debounceRef = React.useRef<ReturnType<typeof setTimeout> | null>(null);
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  const editor = useEditor({
    extensions: [
      StarterKit,
      Underline,
      Link.configure({ openOnClick: false }),
      Image,
      Table.configure({ resizable: true }),
      TableRow,
      TableCell,
      TableHeader,
      TextStyle,
      Color,
    ],
    content: value,
    editable,
    onUpdate: ({ editor: ed }) => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
      debounceRef.current = setTimeout(() => {
        sendCommand('valueChanged', { value: ed.getHTML() });
      }, 300);
    },
  }, [editable]);

  // Sync external value changes into the editor.
  React.useEffect(() => {
    if (editor && !editor.isFocused) {
      const currentHtml = editor.getHTML();
      if (currentHtml !== value) {
        editor.commands.setContent(value, false);
      }
    }
  }, [value, editor]);

  // Handle one-shot imageUrl from server (after upload).
  React.useEffect(() => {
    if (editor && imageUrl) {
      const imgSrc = dataUrl + '&key=' + encodeURIComponent(imageUrl);
      editor.chain().focus().setImage({ src: imgSrc }).run();
    }
  }, [imageUrl, editor, dataUrl]);

  const handleImageUpload = React.useCallback(() => {
    fileInputRef.current?.click();
  }, []);

  const handleFileSelected = React.useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const formData = new FormData();
      formData.append('file', file);
      uploadFile(formData);
    }
    // Reset so the same file can be uploaded again.
    e.target.value = '';
  }, [uploadFile]);

  // Cleanup debounce on unmount.
  React.useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, []);

  if (!editable) {
    return (
      <div className="tlWysiwygEditor tlWysiwygEditor--immutable">
        <div
          className="tlWysiwygEditor__immutableContent"
          dangerouslySetInnerHTML={{ __html: value }}
        />
      </div>
    );
  }

  const cssClass = 'tlWysiwygEditor' + (hasError ? ' tlWysiwygEditor--error' : '');

  return (
    <div className={cssClass}>
      <WysiwygToolbar editor={editor} items={toolbar} onImageUpload={handleImageUpload} />
      <div className="tlWysiwygEditor__content">
        <EditorContent editor={editor} />
      </div>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        style={{ display: 'none' }}
        onChange={handleFileSelected}
      />
    </div>
  );
};

export default TLWysiwygEditor;
```

- [ ] **Step 4: Update entry point**

Replace `com.top_logic.layout.react.wysiwyg/react-src/wysiwyg-entry.ts`:

```typescript
// TipTap WYSIWYG editor registration for the tl-react-wysiwyg bundle.
//
// IMPORTANT: All components MUST import React from 'tl-react-bridge' (not 'react').

import { register } from 'tl-react-bridge';
import TLWysiwygEditor from './controls/TLWysiwygEditor';

register('TLWysiwygEditor', TLWysiwygEditor);
```

- [ ] **Step 5: Build to verify**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react.wysiwyg`
Expected: BUILD SUCCESS (Vite bundles TipTap + component into tl-react-wysiwyg.js)

- [ ] **Step 6: Commit**

```
Ticket #29108: Add TLWysiwygEditor React component with TipTap integration.
```

---

## Task 5: Demo integration

**Files:**
- Modify: `com.top_logic.demo/pom.xml`
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/model/DemoTypes.model.xml:253`

- [ ] **Step 1: Add dependency to demo pom.xml**

In `com.top_logic.demo/pom.xml`, after the `tl-layout-react-chartjs` dependency, add:

```xml
    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-layout-react-wysiwyg</artifactId>
    </dependency>
```

- [ ] **Step 2: Add Html attribute to DemoTypes.A**

In `DemoTypes.model.xml`, inside the `<interface name="DemoTypes.A">` attributes section (after line 252, within the `<attributes>` block), add a new property:

```xml
			<property name="htmlDescription"
				type="tl.model.wysiwyg:Html"
			>
				<annotations>
					<sort-order value="500.0"/>
				</annotations>
			</property>
```

Use `htmlDescription` to avoid conflicts with any existing `description` attributes. Place it at sort-order 500 so it appears after the existing attributes in forms.

- [ ] **Step 3: Build demo module**

Run: `mvn compile -DskipTests=true -pl com.top_logic.demo`

**Important:** Do NOT use `mvn clean` on `com.top_logic.demo` — it causes a PluginContainerException. Use incremental compilation only.

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```
Ticket #29108: Add Html attribute to DemoTypes.A for WYSIWYG editor demo.
```

---

## Task 6: Manual testing

This task is not code — it is a checklist for verifying the editor works in the running demo app.

- [ ] **Step 1: Start the demo app**

Use the `tl-app` skill: start the demo app (`com.top_logic.demo`).

- [ ] **Step 2: Test editable mode**

1. Log in as root/root1234
2. Navigate to a DemoTypes.A object form
3. Verify the `htmlDescription` field shows the TipTap editor with toolbar
4. Type text, apply bold/italic/underline formatting
5. Add a heading, bullet list, ordered list
6. Insert a link
7. Insert a table
8. Save the form — verify the value persists after reload

- [ ] **Step 3: Test image upload**

1. Click the image toolbar button
2. Select an image file
3. Verify the image appears in the editor
4. Save and reload — verify the image persists

- [ ] **Step 4: Test immutable mode**

1. View the object in a read-only context (e.g. table cell or non-editable form)
2. Verify the HTML content renders correctly without the editor toolbar
3. Verify embedded images display correctly

- [ ] **Step 5: Test error state**

1. If the field has validation constraints, trigger an error
2. Verify the editor shows the error border styling
