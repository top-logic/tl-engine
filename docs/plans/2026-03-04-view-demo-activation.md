# View System Demo Activation Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Activate the view system in tl-demo so it can be tested end-to-end by navigating to `/view/` (default view) or `/view/<name>.view.xml`.

**Architecture:** Add a `ViewConfig` configuration interface to `ViewServlet` with a configurable default view path (defaulting to `app.view.xml`). Create a `DemoCounterElement` UIElement in tl-demo that wraps the existing `DemoCounterControl`. Add tl-layout-view as a dependency of tl-demo, and create an `app.view.xml` that exercises all PoC elements (AppShell, Stack, Panel, DemoCounter).

**Tech Stack:** Java 17, TypedConfiguration, ApplicationConfig, ReactControl (existing), Maven.

---

### Task 1: Add ViewConfig to ViewServlet

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewConfig.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java`

**Step 1: Create ViewConfig**

Create `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewConfig.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Application-level configuration for the view system.
 *
 * <p>
 * Registered via {@link ApplicationConfig} and accessible from any module. Applications override
 * properties in their {@code .conf.config.xml} files.
 * </p>
 */
public interface ViewConfig extends ConfigurationItem {

	/** Configuration name for {@link #getDefaultView()}. */
	String DEFAULT_VIEW = "default-view";

	/**
	 * The name of the default view file loaded when the {@link ViewServlet} is accessed without a
	 * path (e.g. {@code /view/}).
	 *
	 * <p>
	 * The file is resolved relative to {@code /WEB-INF/views/} in the webapp.
	 * </p>
	 */
	@Name(DEFAULT_VIEW)
	@StringDefault("app.view.xml")
	String getDefaultView();
}
```

**Step 2: Modify ViewServlet to use ViewConfig for default view**

In `ViewServlet.java`, change `resolveViewPath()` to fall back to the configured default view when no path info is given. Add a helper method to access the config.

Replace the `resolveViewPath` method (lines 115-123):

```java
private String resolveViewPath(HttpServletRequest request) {
	String pathInfo = request.getPathInfo();
	if (pathInfo != null && pathInfo.length() > 1) {
		// Explicit path: /view/foo.view.xml -> /WEB-INF/views/foo.view.xml
		String viewName = pathInfo.substring(1);
		return VIEW_BASE_PATH + viewName;
	}
	// Fall back to the configured default view.
	String defaultView = ApplicationConfig.getInstance().getConfig(ViewConfig.class).getDefaultView();
	return VIEW_BASE_PATH + defaultView;
}
```

Also remove the null check and 400 error for `viewPath == null` in `doGet()` (lines 83-88), since `resolveViewPath` now always returns a value. Replace those lines:

Old code (lines 83-88):
```java
String viewPath = resolveViewPath(request);
if (viewPath == null) {
	response.sendError(HttpServletResponse.SC_BAD_REQUEST,
		"No view path specified. Use /view/<name>.view.xml");
	return;
}
```

New code:
```java
String viewPath = resolveViewPath(request);
```

**Step 3: Verify compilation**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS.

**Step 4: Commit**

```
Ticket #29108: ViewConfig with configurable default view path.
```

---

### Task 2: Create DemoCounterElement in tl-demo

**Files:**
- Create: `com.top_logic.demo/src/main/java/com/top_logic/demo/view/DemoCounterElement.java`
- Create: `com.top_logic.demo/src/main/java/com/top_logic/demo/view/package-info.java`
- Modify: `com.top_logic.demo/pom.xml`

**Step 1: Add tl-layout-view dependency to tl-demo**

In `com.top_logic.demo/pom.xml`, after the existing `tl-layout-react` dependency (line 135-136), add:

```xml
    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-layout-view</artifactId>
    </dependency>
```

**Step 2: Create package-info.java**

Create `com.top_logic.demo/src/main/java/com/top_logic/demo/view/package-info.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Demo {@link com.top_logic.layout.view.UIElement} implementations for the view system.
 */
package com.top_logic.demo.view;
```

**Step 3: Create DemoCounterElement**

Create `com.top_logic.demo/src/main/java/com/top_logic/demo/view/DemoCounterElement.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.demo.react.DemoReactCounterComponent.DemoCounterControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * {@link UIElement} that wraps the {@link DemoCounterControl}.
 *
 * <p>
 * Demonstrates how application-specific elements integrate with the view system by wrapping an
 * existing {@link com.top_logic.layout.react.ReactControl}.
 * </p>
 */
public class DemoCounterElement implements UIElement {

	/**
	 * Configuration for {@link DemoCounterElement}.
	 */
	@TagName("demo-counter")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(DemoCounterElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The display label for the counter, or empty for the default.
		 */
		@Name(LABEL)
		String getLabel();
	}

	private final String _label;

	/**
	 * Creates a new {@link DemoCounterElement} from configuration.
	 */
	@CalledByReflection
	public DemoCounterElement(InstantiationContext context, Config config) {
		_label = config.getLabel();
	}

	@Override
	public Control createControl(ViewContext context) {
		return new DemoCounterControl(_label);
	}
}
```

**Step 4: Verify compilation**

Run: `mvn -f com.top_logic.demo/pom.xml compile -DskipTests=true`

Expected: BUILD SUCCESS.

**Step 5: Commit**

```
Ticket #29108: DemoCounterElement wrapping DemoCounterControl for the view system.
```

---

### Task 3: Create demo view file

**Files:**
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/views/app.view.xml`

**Step 1: Create the views directory and app.view.xml**

Create `com.top_logic.demo/src/main/webapp/WEB-INF/views/app.view.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<content>
		<app-shell>
			<content>
				<stack direction="row" gap="default">
					<children>
						<panel title="Counter A">
							<children>
								<demo-counter label="First"/>
							</children>
						</panel>
						<panel title="Counter B">
							<children>
								<demo-counter label="Second"/>
							</children>
						</panel>
					</children>
				</stack>
			</content>
		</app-shell>
	</content>
</view>
```

**Step 2: Commit**

```
Ticket #29108: Demo view file exercising all PoC view elements.
```

---

### Task 4: Build and verify

**Step 1: Build the view module**

Run: `mvn -f com.top_logic.layout.view/pom.xml clean install`

Expected: BUILD SUCCESS.

**Step 2: Build the demo module**

Run: `mvn -f com.top_logic.demo/pom.xml clean compile -DskipTests=true`

Expected: BUILD SUCCESS.

**Step 3: Verify no regressions in existing test**

Run: `mvn -f com.top_logic.layout.view/pom.xml test -DskipTests=false -Dtest=TestViewElement`

Expected: Test passes.

**Step 4: Commit any adjustments**

```
Ticket #29108: Build verification for view system demo activation.
```

---

## Summary

| Task | What | Key Files |
|------|------|-----------|
| 1 | ViewConfig + default view fallback | `ViewConfig.java`, `ViewServlet.java` |
| 2 | DemoCounterElement + tl-demo dependency | `DemoCounterElement.java`, `pom.xml` |
| 3 | Demo view XML | `app.view.xml` |
| 4 | Build verification | Full build |

**Access URL:** `http://localhost:8080/tl-demo/view/` (loads default `app.view.xml`)
