# View System PoC Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Create a new `com.top_logic.layout.view` module with the UIElement configuration layer that produces per-session ReactControl trees from shared, stateless view definitions.

**Architecture:** View files (`.view.xml`) are parsed via TypedConfiguration into UIElement config trees. UIElement instances are created once (shared across sessions) and act as factories that produce ReactControl trees per session. A new ViewServlet bootstraps the initial HTML page; all subsequent interaction goes through the existing ReactServlet/SSE pipeline.

**Tech Stack:** Java 17, TypedConfiguration, ReactControl (existing), Maven module with `tl-parent-core-internal` parent.

---

### Task 1: Create Maven Module

**Files:**
- Create: `com.top_logic.layout.view/pom.xml`
- Modify: `tl-parent-engine/pom.xml` (add module reference, around line 85)

**Step 1: Create module directory and POM**

Create `com.top_logic.layout.view/pom.xml`:

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

  <artifactId>tl-layout-view</artifactId>
  <name>${project.artifactId}</name>
  <description>Declarative view configuration layer for TopLogic UI.</description>
  <url>https://github.com/top-logic/tl-engine/</url>

  <dependencies>
    <dependency>
      <groupId>com.top-logic</groupId>
      <artifactId>tl-layout-react</artifactId>
    </dependency>
  </dependencies>
</project>
```

**Step 2: Register module in engine aggregator**

In `tl-parent-engine/pom.xml`, after the line `<module>../com.top_logic.layout.react</module>`, add:

```xml
<module>../com.top_logic.layout.view</module>
```

**Step 3: Create source directory structure**

```
com.top_logic.layout.view/
  src/main/java/
    com/top_logic/layout/view/
    META-INF/
  src/main/webapp/
    WEB-INF/conf/
```

**Step 4: Verify the module compiles**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS (empty module compiles).

**Step 5: Commit**

```
Ticket #29108: Create com.top_logic.layout.view Maven module.
```

---

### Task 2: UIElement Base Interface

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/UIElement.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/package-info.java`

**Step 1: Create package-info.java**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Declarative view configuration layer.
 *
 * <p>
 * A view is a tree of {@link com.top_logic.layout.view.UIElement} configurations parsed from
 * {@code .view.xml} files. The UIElement tree is shared across all sessions (stateless factory).
 * Each session gets its own {@link com.top_logic.layout.Control} tree by calling
 * {@link com.top_logic.layout.view.UIElement#createControl(ViewContext)}.
 * </p>
 */
package com.top_logic.layout.view;
```

**Step 2: Create ViewContext**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.layout.FrameScope;
import com.top_logic.layout.react.SSEUpdateQueue;

/**
 * Hierarchical context for UIElement control creation.
 *
 * <p>
 * Provides session-scoped infrastructure needed to create and wire controls. Container elements
 * may create derived contexts that add scoped information for their children.
 * </p>
 */
public class ViewContext {

	private final FrameScope _frameScope;

	private final SSEUpdateQueue _sseQueue;

	/**
	 * Creates a new {@link ViewContext}.
	 *
	 * @param frameScope
	 *        Scope for allocating control IDs.
	 * @param sseQueue
	 *        Queue for SSE event delivery.
	 */
	public ViewContext(FrameScope frameScope, SSEUpdateQueue sseQueue) {
		_frameScope = frameScope;
		_sseQueue = sseQueue;
	}

	/**
	 * The frame scope for allocating control IDs.
	 */
	public FrameScope getFrameScope() {
		return _frameScope;
	}

	/**
	 * The SSE update queue for the current session.
	 */
	public SSEUpdateQueue getSSEQueue() {
		return _sseQueue;
	}
}
```

**Step 3: Create UIElement**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.Control;

/**
 * Base interface for all view elements.
 *
 * <p>
 * A UIElement is a stateless factory for {@link Control}. The UIElement tree is shared across all
 * sessions (parsed once from configuration). Each session gets its own control tree by calling
 * {@link #createControl(ViewContext)}.
 * </p>
 */
public interface UIElement {

	/**
	 * Configuration interface for {@link UIElement}.
	 *
	 * <p>
	 * Every concrete UIElement has a corresponding Config sub-interface. Because this extends
	 * {@link PolymorphicConfiguration}, new element types can be registered in any module.
	 * </p>
	 */
	interface Config extends PolymorphicConfiguration<UIElement> {
		// Common properties (css-class, visibility, etc.) will be added here later.
	}

	/**
	 * Creates a {@link Control} for this element in the given session context.
	 *
	 * @param context
	 *        The view context providing session-scoped infrastructure.
	 * @return A control for the current session. Typically a
	 *         {@link com.top_logic.layout.react.ReactControl}.
	 */
	Control createControl(ViewContext context);
}
```

**Step 4: Verify compilation**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS.

**Step 5: Commit**

```
Ticket #29108: UIElement base interface and ViewContext.
```

---

### Task 3: ContainerElement Base Class

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ContainerElement.java`

**Step 1: Create ContainerElement**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.Control;

/**
 * Base class for UIElements that hold a list of child elements.
 */
public abstract class ContainerElement implements UIElement {

	/**
	 * Configuration for {@link ContainerElement}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * The child elements.
		 */
		@Name(CHILDREN)
		List<PolymorphicConfiguration<? extends UIElement>> getChildren();
	}

	private final List<UIElement> _children;

	/**
	 * Creates a new {@link ContainerElement} from configuration.
	 */
	@CalledByReflection
	protected ContainerElement(InstantiationContext context, Config config) {
		_children = config.getChildren().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	/**
	 * The child elements (shared, stateless).
	 */
	protected List<UIElement> getChildren() {
		return _children;
	}

	/**
	 * Creates controls for all children.
	 */
	protected List<Control> createChildControls(ViewContext context) {
		return _children.stream()
			.map(child -> child.createControl(context))
			.collect(Collectors.toList());
	}
}
```

**Step 2: Verify compilation**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS.

**Step 3: Commit**

```
Ticket #29108: ContainerElement base class with child list.
```

---

### Task 4: ViewElement - File Root

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java`

**Step 1: Create ViewElement**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.Mandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.Control;

/**
 * The mandatory root element of every {@code .view.xml} file.
 *
 * <p>
 * Establishes the scope boundary for a view. In the future, this is where channel declarations
 * and view-level configuration will be defined.
 * </p>
 */
public class ViewElement implements UIElement {

	/**
	 * Configuration for {@link ViewElement}.
	 */
	@TagName("view")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * The root content element of this view.
		 */
		@Name(CONTENT)
		@Mandatory
		PolymorphicConfiguration<? extends UIElement> getContent();
	}

	private final UIElement _content;

	/**
	 * Creates a new {@link ViewElement} from configuration.
	 */
	@CalledByReflection
	public ViewElement(InstantiationContext context, Config config) {
		_content = context.getInstance(config.getContent());
	}

	@Override
	public Control createControl(ViewContext context) {
		return _content.createControl(context);
	}
}
```

**Step 2: Verify compilation**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS.

**Step 3: Commit**

```
Ticket #29108: ViewElement as mandatory view file root.
```

---

### Task 5: Concrete Element Implementations

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/AppShellElement.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/PanelElement.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/StackElement.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/package-info.java`

**Step 1: Create element package-info**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Built-in UIElement implementations that wrap existing ReactControls.
 */
package com.top_logic.layout.view.element;
```

**Step 2: Create AppShellElement**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.Mandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.Control;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.control.nav.ReactAppShellControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * Application shell element with header, content, and footer slots.
 *
 * <p>
 * Wraps {@link ReactAppShellControl}.
 * </p>
 */
public class AppShellElement implements UIElement {

	/**
	 * Configuration for {@link AppShellElement}.
	 */
	@TagName("app-shell")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getHeader()}. */
		String HEADER = "header";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/** Configuration name for {@link #getFooter()}. */
		String FOOTER = "footer";

		/**
		 * Optional header element (e.g., an app bar).
		 */
		@Name(HEADER)
		PolymorphicConfiguration<? extends UIElement> getHeader();

		/**
		 * The main content element.
		 */
		@Name(CONTENT)
		@Mandatory
		PolymorphicConfiguration<? extends UIElement> getContent();

		/**
		 * Optional footer element (e.g., a bottom bar).
		 */
		@Name(FOOTER)
		PolymorphicConfiguration<? extends UIElement> getFooter();
	}

	private final UIElement _header;

	private final UIElement _content;

	private final UIElement _footer;

	/**
	 * Creates a new {@link AppShellElement} from configuration.
	 */
	@CalledByReflection
	public AppShellElement(InstantiationContext context, Config config) {
		_header = config.getHeader() != null ? context.getInstance(config.getHeader()) : null;
		_content = context.getInstance(config.getContent());
		_footer = config.getFooter() != null ? context.getInstance(config.getFooter()) : null;
	}

	@Override
	public Control createControl(ViewContext context) {
		ReactControl header = _header != null ? (ReactControl) _header.createControl(context) : null;
		ReactControl content = (ReactControl) _content.createControl(context);
		ReactControl footer = _footer != null ? (ReactControl) _footer.createControl(context) : null;
		return new ReactAppShellControl(header, content, footer);
	}
}
```

**Step 3: Create PanelElement**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.Control;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.ViewContext;

/**
 * A titled content panel with toolbar header.
 *
 * <p>
 * Wraps {@link ReactPanelControl}. If there are multiple children, they are arranged in a
 * vertical {@link ReactStackControl}.
 * </p>
 */
public class PanelElement extends ContainerElement {

	/**
	 * Configuration for {@link PanelElement}.
	 */
	@TagName("panel")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/**
		 * The panel title displayed in the toolbar header.
		 */
		@Name(TITLE)
		String getTitle();
	}

	private final Config _config;

	/**
	 * Creates a new {@link PanelElement} from configuration.
	 */
	@CalledByReflection
	public PanelElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
	}

	@Override
	public Control createControl(ViewContext context) {
		List<Control> childControls = createChildControls(context);

		ReactControl content;
		if (childControls.size() == 1) {
			content = (ReactControl) childControls.get(0);
		} else {
			List<ReactControl> reactChildren = childControls.stream()
				.map(c -> (ReactControl) c)
				.toList();
			content = new ReactStackControl(reactChildren);
		}

		return new ReactPanelControl(_config.getTitle(), content, false, false, false);
	}
}
```

**Step 4: Create StackElement**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.Control;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.ViewContext;

/**
 * A flexbox container that arranges children vertically or horizontally.
 *
 * <p>
 * Wraps {@link ReactStackControl}.
 * </p>
 */
public class StackElement extends ContainerElement {

	/**
	 * Configuration for {@link StackElement}.
	 */
	@TagName("stack")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getDirection()}. */
		String DIRECTION = "direction";

		/** Configuration name for {@link #getGap()}. */
		String GAP = "gap";

		/** Configuration name for {@link #getAlign()}. */
		String ALIGN = "align";

		/**
		 * Layout direction: "column" or "row".
		 */
		@Name(DIRECTION)
		@StringDefault("column")
		String getDirection();

		/**
		 * Gap between children: "compact", "default", or "loose".
		 */
		@Name(GAP)
		@StringDefault("default")
		String getGap();

		/**
		 * Cross-axis alignment: "start", "center", "end", or "stretch".
		 */
		@Name(ALIGN)
		@StringDefault("stretch")
		String getAlign();
	}

	private final Config _config;

	/**
	 * Creates a new {@link StackElement} from configuration.
	 */
	@CalledByReflection
	public StackElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
	}

	@Override
	public Control createControl(ViewContext context) {
		List<ReactControl> reactChildren = createChildControls(context).stream()
			.map(c -> (ReactControl) c)
			.toList();
		return new ReactStackControl(
			_config.getDirection(), _config.getGap(), _config.getAlign(), false, reactChildren);
	}
}
```

**Step 5: Verify compilation**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS.

**Step 6: Commit**

```
Ticket #29108: AppShellElement, PanelElement, and StackElement implementations.
```

---

### Task 6: ViewServlet - Bootstrap Entry Point

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java`
- Create: `com.top_logic.layout.view/src/main/java/META-INF/web-fragment.xml`

**Context:** The ViewServlet needs to:
1. Resolve the view path from URL or default config
2. Load and cache the `ViewElement.Config` from the `.view.xml` file
3. Instantiate the UIElement tree (shared)
4. Create the per-session control tree
5. Render the bootstrap HTML page that mounts the React root

**Key references:**
- `ReactServlet` (`com.top_logic.layout.react.ReactServlet`) extends `TopLogicServlet` for
  session/display context setup
- `ReactControl.internalWrite()` (line 224-258 of ReactControl.java) shows how the mount-point
  div and bootstrap script are rendered
- `SSEUpdateQueue.forSession()` gets/creates session-scoped queue

**Step 1: Create web-fragment.xml**

Create `com.top_logic.layout.view/src/main/java/META-INF/web-fragment.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-fragment
	xmlns="http://java.sun.com/xml/ns/javaee"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee/web-fragment_3_0.xsd"
	id="tl-layout-view"
	version="3.0"
	metadata-complete="true"
>
	<name>tl-layout-view</name>

	<servlet>
		<servlet-name>ViewServlet</servlet-name>
		<servlet-class>com.top_logic.layout.view.ViewServlet</servlet-class>
	</servlet>

	<servlet-mapping>
		<servlet-name>ViewServlet</servlet-name>
		<url-pattern>/view/*</url-pattern>
	</servlet-mapping>

</web-fragment>
```

**Step 2: Create ViewServlet**

This is the most complex class. The servlet must:
- Parse `.view.xml` via `ConfigurationReader` into `ViewElement.Config`
- Cache the parsed config + instantiated UIElement per view path
- Create a per-session control tree
- Render a full HTML page with:
  - React runtime scripts (same as existing React infrastructure)
  - The root control's mount-point div and bootstrap script

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.SSEUpdateQueue;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.TopLogicServlet;

/**
 * Servlet that bootstraps the view-based UI.
 *
 * <p>
 * Serves the initial HTML page for a view definition. The view file is parsed once and the
 * UIElement tree is cached (shared across sessions). Each session gets its own control tree.
 * After initial render, all interaction is handled by the existing ReactServlet/SSE pipeline.
 * </p>
 */
public class ViewServlet extends TopLogicServlet {

	private final Map<String, ViewElement> _viewCache = new ConcurrentHashMap<>();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(true);

		String viewPath = resolveViewPath(request);
		if (viewPath == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No view specified.");
			return;
		}

		try {
			ViewElement view = getOrLoadView(viewPath);

			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
			FrameScope frameScope = displayContext.getExecutionScope().getFrameScope();
			SSEUpdateQueue sseQueue = SSEUpdateQueue.forSession(session);

			ViewContext viewContext = new ViewContext(frameScope, sseQueue);
			Control rootControl = view.createControl(viewContext);

			renderPage(displayContext, response, (ReactControl) rootControl, frameScope, sseQueue);
		} catch (ConfigurationException ex) {
			Logger.error("Failed to load view: " + viewPath, ex, ViewServlet.class);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Failed to load view: " + ex.getMessage());
		}
	}

	private String resolveViewPath(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		if (pathInfo != null && pathInfo.length() > 1) {
			// Strip leading slash: /path/to/view.xml -> path/to/view.xml
			return pathInfo.substring(1);
		}
		// TODO: Fall back to application-configured default view.
		return null;
	}

	private ViewElement getOrLoadView(String viewPath) throws ConfigurationException {
		ViewElement cached = _viewCache.get(viewPath);
		if (cached != null) {
			return cached;
		}

		ViewElement view = loadView(viewPath);
		_viewCache.put(viewPath, view);
		return view;
	}

	private ViewElement loadView(String viewPath) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(ViewServlet.class);

		// Parse the .view.xml into a ViewElement.Config using TypedConfiguration.
		Map<String, com.top_logic.basic.config.ConfigurationDescriptor> descriptors = Map.of(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = FileUtilities.markupContent(viewPath);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();

		// Instantiate the UIElement tree (Phase 1 - shared).
		ViewElement view = context.getInstance(config);
		context.checkErrors();

		return view;
	}

	private void renderPage(DisplayContext displayContext, HttpServletResponse response,
			ReactControl rootControl, FrameScope frameScope, SSEUpdateQueue sseQueue) throws IOException {
		response.setContentType("text/html;charset=UTF-8");

		try (TagWriter out = new TagWriter(response.getWriter())) {
			out.writeText("<!DOCTYPE html>");
			out.beginBeginTag("html");
			out.writeAttribute("lang", "en");
			out.endBeginTag();

			out.beginTag("head");
			out.beginBeginTag("meta");
			out.writeAttribute("charset", "UTF-8");
			out.endEmptyTag();
			out.beginBeginTag("meta");
			out.writeAttribute("name", "viewport");
			out.writeAttribute("content", "width=device-width, initial-scale=1.0");
			out.endEmptyTag();
			out.beginTag("title");
			out.writeText("TopLogic View");
			out.endTag("title");

			// Include React runtime and TL bridge scripts.
			writeScriptTag(out, displayContext.getContextPath() + "/script/tl-react-bridge.js", true);
			writeScriptTag(out, displayContext.getContextPath() + "/script/tl-react-controls.js", true);
			out.endTag("head");

			out.beginBeginTag("body");
			out.writeAttribute("style", "margin:0;padding:0;height:100vh;");
			out.endBeginTag();

			// Render the root control (writes mount-point div + bootstrap script).
			rootControl.write(displayContext, out);

			out.endTag("body");
			out.endTag("html");
		}
	}

	private void writeScriptTag(TagWriter out, String src, boolean module) throws IOException {
		out.beginBeginTag("script");
		if (module) {
			out.writeAttribute("type", "module");
		}
		out.writeAttribute("src", src);
		out.endBeginTag();
		out.endTag("script");
	}
}
```

**Important notes for the implementer:**
- The `renderPage` method writes a minimal HTML shell. The actual control rendering is delegated
  to `rootControl.write(displayContext, out)` which calls `ReactControl.internalWrite()`.
- `ReactControl.internalWrite()` needs the control to be attached to a scope
  (`getScope().getFrameScope()`). The ViewServlet must ensure the control is properly attached.
  This may require adapting the approach - check how ReactControl gets its scope. The control
  needs `setControlScope(scope)` or equivalent before `write()` is called. Look at how existing
  code attaches controls to scopes.
- The `loadView` method uses `FileUtilities.markupContent()` to resolve the view file. This may
  need adjustment depending on how TopLogic resolves webapp resources. Check `FileManager` as an
  alternative for resolving files from the webapp classpath.

**Step 3: Verify compilation**

Run: `mvn -f com.top_logic.layout.view/pom.xml compile`

Expected: BUILD SUCCESS. Some methods may need adjustment based on actual API signatures.

**Step 4: Commit**

```
Ticket #29108: ViewServlet bootstrap entry point.
```

---

### Task 7: Module Configuration

**Files:**
- Create: `com.top_logic.layout.view/src/main/webapp/WEB-INF/conf/tl-layout-view.conf.config.xml`

**Step 1: Create module configuration**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<application xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<services>
		<config service-class="com.top_logic.basic.util.ResourcesModule">
			<instance>
				<bundles>
					<bundle name="tl-layout-view.messages"/>
				</bundles>
			</instance>
		</config>
	</services>
</application>
```

**Step 2: Create i18n message files (empty for now)**

Create `com.top_logic.layout.view/src/main/webapp/WEB-INF/conf/resources/tl-layout-view.messages_en.properties`:
```
# TopLogic View - English messages
```

Create `com.top_logic.layout.view/src/main/webapp/WEB-INF/conf/resources/tl-layout-view.messages_de.properties`:
```
# TopLogic View - German messages
```

**Step 3: Commit**

```
Ticket #29108: Module configuration and i18n resources.
```

---

### Task 8: Example View File and Integration Test

**Files:**
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/TestViewElement.java`
- Create: `com.top_logic.layout.view/src/test/resources/test-view.view.xml`

**Step 1: Create a test view XML**

Create `com.top_logic.layout.view/src/test/resources/test-view.view.xml`:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
  <content class="com.top_logic.layout.view.element.AppShellElement">
    <content class="com.top_logic.layout.view.element.PanelElement">
      <title>Test Panel</title>
      <children>
        <config config:interface="com.top_logic.layout.view.element.StackElement$Config"
                direction="row"
                gap="compact">
          <children>
            <config config:interface="com.top_logic.layout.view.element.PanelElement$Config">
              <title>Left</title>
              <children/>
            </config>
            <config config:interface="com.top_logic.layout.view.element.PanelElement$Config">
              <title>Right</title>
              <children/>
            </config>
          </children>
        </config>
      </children>
    </content>
  </content>
</view>
```

**Note:** The exact XML syntax depends on how TypedConfiguration resolves tag names. If
`@TagName` annotations work within the descriptor map, the syntax will be simpler (e.g.,
`<stack direction="row">` instead of `<config config:interface="...StackElement$Config">`).
The test will clarify the correct syntax.

**Step 2: Write a unit test for the two-phase lifecycle**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.view.ViewElement;

/**
 * Tests the two-phase UIElement lifecycle: Config parsing -> UIElement instantiation.
 *
 * <p>
 * Phase 2 (control creation) requires a full session context and is tested separately.
 * </p>
 */
public class TestViewElement extends TestCase {

	/**
	 * Tests that a view XML file can be parsed into a ViewElement config and instantiated.
	 */
	public void testParseAndInstantiate() throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestViewElement.class);

		var descriptors = java.util.Map.of(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = FileUtilities.markupContent("test-view.view.xml");
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);
		assertNotNull("Content should be set", config.getContent());

		// Phase 1: Instantiate UIElement tree (shared, stateless).
		ViewElement view = context.getInstance(config);
		context.checkErrors();
		assertNotNull("ViewElement should be instantiated", view);
	}
}
```

**Step 3: Run the test**

Run: `mvn -f com.top_logic.layout.view/pom.xml test -DskipTests=false -Dtest=TestViewElement`

Expected: Test passes, proving Phase 1 (config parsing + UIElement instantiation) works.
If it fails, adjust the XML syntax based on error messages. Common issues:
- Tag name resolution (may need explicit `config:interface` attributes)
- File resolution (may need to adjust the `FileUtilities.markupContent` call path)
- Missing TypedConfiguration descriptors

**Step 4: Commit**

```
Ticket #29108: Test for view XML parsing and UIElement instantiation.
```

---

### Task 9: Final Build Verification

**Step 1: Full module build**

Run: `mvn -f com.top_logic.layout.view/pom.xml clean install`

Expected: BUILD SUCCESS with all tests passing.

**Step 2: Verify no regressions in React module**

Run: `mvn -f com.top_logic.layout.react/pom.xml compile`

Expected: BUILD SUCCESS (no changes to React module).

**Step 3: Commit any adjustments, push**

```
Ticket #29108: View system PoC complete.
```

---

## Summary

| Task | What | Key Files |
|------|------|-----------|
| 1 | Maven module | `pom.xml`, `tl-parent-engine/pom.xml` |
| 2 | UIElement + ViewContext | `UIElement.java`, `ViewContext.java` |
| 3 | ContainerElement | `ContainerElement.java` |
| 4 | ViewElement | `ViewElement.java` |
| 5 | Concrete elements | `AppShellElement.java`, `PanelElement.java`, `StackElement.java` |
| 6 | ViewServlet | `ViewServlet.java`, `web-fragment.xml` |
| 7 | Module config | `tl-layout-view.conf.config.xml`, messages |
| 8 | Test | `TestViewElement.java`, `test-view.view.xml` |
| 9 | Final verification | Full build |
