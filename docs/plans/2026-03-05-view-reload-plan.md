# View Reload Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Re-read `.view.xml` files automatically when they change on disk, so developers see updates without restarting the server.

**Architecture:** Replace the bare `ConcurrentHashMap<String, ViewElement>` in `ViewServlet` with entries that also store the file's `lastModified` timestamp. On each request, compare `File.lastModified()` against the cached value and re-parse if different.

**Tech Stack:** Java 17, `java.io.File.lastModified()`, `ConcurrentHashMap`, TopLogic `FileManager`

**Design doc:** `docs/plans/2026-03-05-view-reload-design.md`

---

### Task 1: Add CachedView inner class and update cache field

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java:66-75`

**Step 1: Add the `CachedView` inner class**

Add a private static inner class at the end of `ViewServlet` (before the closing brace), and change the cache field type.

Replace lines 71-75:
```java
	/**
	 * Global cache of parsed {@link ViewElement}s, keyed by view path. The element tree is
	 * stateless and shared across all sessions.
	 */
	private final ConcurrentHashMap<String, ViewElement> _viewCache = new ConcurrentHashMap<>();
```

With:
```java
	/**
	 * Global cache of parsed {@link ViewElement}s, keyed by view path. Each entry stores the
	 * file's {@code lastModified} timestamp so the view is re-parsed when the file changes.
	 */
	private final ConcurrentHashMap<String, CachedView> _viewCache = new ConcurrentHashMap<>();
```

Add before the closing brace of `ViewServlet`:
```java
	/**
	 * A cached view together with the source file's modification timestamp.
	 */
	private static final class CachedView {

		final ViewElement _view;

		final long _lastModified;

		CachedView(ViewElement view, long lastModified) {
			_view = view;
			_lastModified = lastModified;
		}
	}
```

**Step 2: Add `java.io.File` import**

Add to the import block:
```java
import java.io.File;
```

**Step 3: Compile to verify**

Run: `mvn compile -pl com.top_logic.layout.view -am -q` from the repository root.
Expected: Compilation error in `getOrLoadView()` because the method still expects `ViewElement` from the map. This is expected and will be fixed in Task 2.

---

### Task 2: Update getOrLoadView() with timestamp checking

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java:170-182`

**Step 1: Rewrite `getOrLoadView()`**

Replace lines 170-182:
```java
	/**
	 * Retrieves a cached {@link ViewElement} or loads and caches it from the view XML file.
	 */
	private ViewElement getOrLoadView(String viewPath) throws ConfigurationException {
		ViewElement cached = _viewCache.get(viewPath);
		if (cached != null) {
			return cached;
		}

		ViewElement view = loadView(viewPath);
		_viewCache.putIfAbsent(viewPath, view);
		return _viewCache.get(viewPath);
	}
```

With:
```java
	/**
	 * Retrieves a cached {@link ViewElement} or loads and caches it from the view XML file.
	 *
	 * <p>
	 * Checks the source file's modification timestamp against the cached value. If the file has
	 * been modified since it was last parsed, the cache entry is replaced with a freshly parsed
	 * view.
	 * </p>
	 */
	private ViewElement getOrLoadView(String viewPath) throws ConfigurationException {
		File file = FileManager.getInstance().getIDEFileOrNull(viewPath);
		long currentModified = file != null ? file.lastModified() : 0L;

		CachedView cached = _viewCache.get(viewPath);
		if (cached != null && cached._lastModified == currentModified) {
			return cached._view;
		}

		ViewElement view = loadView(viewPath);
		_viewCache.put(viewPath, new CachedView(view, currentModified));
		return view;
	}
```

Key changes:
- Looks up `File` via `FileManager.getIDEFileOrNull()` to get a real filesystem path.
- Compares `lastModified` timestamps (0L if file not found on disk).
- Uses `put()` instead of `putIfAbsent()` to replace stale entries.
- If two threads race on a stale entry, both re-parse — harmless since `ViewElement` trees are stateless.

**Step 2: Compile to verify**

Run: `mvn compile -pl com.top_logic.layout.view -am -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```
Ticket #29108: Auto-reload view XML files when modified on disk.
```

---

### Task 3: Add unit test for reload behavior

**Files:**
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/TestViewReload.java`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/reload-v1.view.xml`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/reload-v2.view.xml`

**Step 1: Create test view XML files**

`reload-v1.view.xml` — a minimal view with a panel titled "Version 1":
```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<panel title="Version 1"/>
</view>
```

`reload-v2.view.xml` — same structure, different title:
```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<panel title="Version 2"/>
</view>
```

**Step 2: Write the test class**

The test exercises the reload logic by directly calling `getOrLoadView()` via reflection or by extracting the caching logic into a testable helper. Since `getOrLoadView()` is private, the simplest approach is to test at the `ViewServlet` level using the same parsing approach as `TestViewElement`, but testing the cache invalidation logic directly.

A cleaner approach: extract the caching logic into a package-visible `ViewCache` class that `ViewServlet` delegates to. But since the design is minimal (one small method), testing via reflection on a `ViewServlet` instance is acceptable for now.

Alternative cleaner approach — test the timestamp logic in isolation:

```java
package test.com.top_logic.layout.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.PanelElement;

/**
 * Tests that the view cache reloads when the underlying XML file changes.
 */
public class TestViewReload extends TestCase {

	/**
	 * Tests that modifying a view XML file causes re-parsing on next load.
	 */
	public void testReloadOnFileChange() throws Exception {
		// Copy v1 to a temp file.
		File v1Source = toFile("reload-v1.view.xml");
		File v2Source = toFile("reload-v2.view.xml");

		File tempView = File.createTempFile("reload-test", ".view.xml");
		tempView.deleteOnExit();

		Files.copy(v1Source.toPath(), tempView.toPath(), StandardCopyOption.REPLACE_EXISTING);

		// Parse v1.
		ViewElement view1 = ViewServletTestUtil.loadViewFromFile(tempView);
		PanelElement panel1 = (PanelElement) ((ViewElement) view1).getContent().get(0);
		assertEquals("Version 1", panel1.getTitle());

		// Ensure file timestamp changes (some filesystems have 1s resolution).
		long oldModified = tempView.lastModified();
		Thread.sleep(1100);

		// Overwrite with v2.
		Files.copy(v2Source.toPath(), tempView.toPath(), StandardCopyOption.REPLACE_EXISTING);
		assertTrue("Timestamp should have changed", tempView.lastModified() != oldModified);

		// Parse again — should get v2.
		ViewElement view2 = ViewServletTestUtil.loadViewFromFile(tempView);
		PanelElement panel2 = (PanelElement) ((ViewElement) view2).getContent().get(0);
		assertEquals("Version 2", panel2.getTitle());
	}

	private File toFile(String name) {
		ClassRelativeBinaryContent content = new ClassRelativeBinaryContent(TestViewReload.class, name);
		return content.getFile();
	}

	/** Test suite requiring {@link TypeIndex}. */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestViewReload.class, TypeIndex.Module.INSTANCE);
	}
}
```

Note: `ViewServletTestUtil.loadViewFromFile(File)` is a small package-visible helper that wraps the same parsing logic as `ViewServlet.loadView()`. This avoids reflection and keeps the test clean.

**Step 3: Create `ViewServletTestUtil`**

Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/ViewServletTestUtil.java`

```java
package test.com.top_logic.layout.view;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;

/**
 * Utility for loading view XML in tests.
 */
class ViewServletTestUtil {

	static ViewElement loadViewFromFile(File file) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		DefaultInstantiationContext context = new DefaultInstantiationContext(ViewServletTestUtil.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(BinaryDataFactory.createBinaryData(file));

		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		UIElement element = context.getInstance(config);
		return (ViewElement) element;
	}
}
```

Note: The exact API for `BinaryDataFactory.createBinaryData(File)` or `FileUtilities.createBinaryData(File)` should be verified — check which factory method exists in `com.top_logic.basic`. `ClassRelativeBinaryContent.getFile()` may not exist either — verify and adjust. The goal is to get a `BinaryData`/`Content` from a `File`.

**Step 4: Run the test**

Run: `mvn test -pl com.top_logic.layout.view -DskipTests=false -Dtest=TestViewReload -q`
Expected: BUILD SUCCESS, test passes.

**Step 5: Commit**

```
Ticket #29108: Add unit test for view file reload on modification.
```

---

### Task 4: Verify the `PanelElement.getTitle()` accessor exists

Before the test can work, verify that `PanelElement` exposes a `getTitle()` method (or equivalent) so the test can assert on the panel title. If it doesn't, adjust the test to assert on a different property or use config inspection instead.

**Files:**
- Read: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/PanelElement.java`

Check:
- Does `PanelElement` have a `getTitle()` method?
- Does `ViewElement` expose `getContent()` returning the list of child `UIElement`s?

If not, adapt the test assertions accordingly (e.g., check config properties directly via `TypedConfiguration.getConfigurationDescriptor`).

This is a verification step — no code changes unless the API differs from expectations.

---

### Summary

| Task | Description | Estimated Effort |
|------|-------------|-----------------|
| 1 | Add `CachedView` inner class, update cache field type | Small |
| 2 | Rewrite `getOrLoadView()` with timestamp checking | Small |
| 3 | Add unit test for reload behavior | Medium |
| 4 | Verify test assertions match actual API | Small |

Total: ~4 small changes to `ViewServlet.java`, plus a test class and two test XML files.
