/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;

import com.top_logic.basic.io.PathUpdate;
import com.top_logic.mig.html.layout.LayoutUpdate;

/**
 * Tests the layout invalidation derived from a {@link PathUpdate} by {@link LayoutUpdate}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestLayoutUpdate extends TestCase {

	private static final Path LAYOUTS =
		Paths.get("/workspace/some.module/src/main/webapp/WEB-INF/layouts");

	/**
	 * A written layout invalidates that layout alone.
	 */
	public void testLayout() {
		assertInvalidates("demo/structure/index.layout.xml", creation("demo/structure/index.layout.xml"));
		assertInvalidates("demo/structure/index.layout.xml", change("demo/structure/index.layout.xml"));
		assertInvalidates("demo/structure/index.layout.xml", deletion("demo/structure/index.layout.xml"));
	}

	/**
	 * An overlay invalidates the layout it overrides.
	 */
	public void testOverlay() {
		assertInvalidates("demo/structure/index.layout.xml", change("demo/structure/index.layout.overlay.xml"));
	}

	/**
	 * A resource a layout can be composed of invalidates all layouts, since the layouts using it are
	 * not known.
	 */
	public void testOtherLayoutResource() {
		assertReloadsEverything(change("templates/contentTab.xml"));
		assertReloadsEverything(creation("demo/structure/dialog.window.xml"));
	}

	/**
	 * A deleted directory invalidates all layouts: It can no longer be inspected, so the layouts it
	 * contained are unknown.
	 */
	public void testDeletedDirectory() {
		assertReloadsEverything(deletion("demo/structure/testLayoutExport"));
	}

	/**
	 * A template is loaded separately and invalidates no layout.
	 */
	public void testTemplate() {
		assertInvalidatesNothing(change("demo/structure/tab.template.xml"));
	}

	/**
	 * A file the layout loader does not read invalidates nothing - not even when it carries a layout
	 * suffix in the middle of its name, as a temporary or backup copy does.
	 */
	public void testUnrelatedFile() {
		assertInvalidatesNothing(creation("demo/structure/index.layout.xml12345.tmp"));
		assertInvalidatesNothing(deletion("demo/structure/index.layout.xml12345.tmp"));
		assertInvalidatesNothing(change("demo/structure/index.layout.xml.bak"));
		assertInvalidatesNothing(creation("demo/structure/index.template.xml.orig"));
		assertInvalidatesNothing(change("README.md"));
	}

	/**
	 * A change outside the layout directory invalidates nothing.
	 */
	public void testOutsideLayoutDirectory() {
		Path path = Paths.get("/workspace/some.module/src/main/webapp/WEB-INF/conf/top-logic.config.xml");

		assertInvalidatesNothing(new PathUpdate(Collections.emptySet(), Collections.singleton(path),
			Collections.emptySet()));
	}

	private void assertInvalidates(String expectedLayoutKey, PathUpdate update) {
		LayoutUpdate layoutUpdate = new LayoutUpdate(update);

		assertTrue("Must not reload everything for " + update, layoutUpdate.canIncrementalUpdate());
		assertTrue(layoutUpdate.hasChanges());
		assertEquals(Collections.singleton(expectedLayoutKey), new HashSet<>(layoutUpdate.getInvalidLayoutKeys()));
	}

	private void assertInvalidatesNothing(PathUpdate update) {
		LayoutUpdate layoutUpdate = new LayoutUpdate(update);

		assertTrue("Must not reload everything for " + update, layoutUpdate.canIncrementalUpdate());
		assertEquals(Collections.emptySet(), new HashSet<>(layoutUpdate.getInvalidLayoutKeys()));
		assertFalse(layoutUpdate.hasChanges());
	}

	private void assertReloadsEverything(PathUpdate update) {
		LayoutUpdate layoutUpdate = new LayoutUpdate(update);

		assertFalse(layoutUpdate.canIncrementalUpdate());
		assertTrue(layoutUpdate.hasChanges());
	}

	private PathUpdate creation(String layoutLocalPath) {
		return new PathUpdate(Collections.singleton(layoutPath(layoutLocalPath)), Collections.emptySet(),
			Collections.emptySet());
	}

	private PathUpdate change(String layoutLocalPath) {
		return new PathUpdate(Collections.emptySet(), Collections.singleton(layoutPath(layoutLocalPath)),
			Collections.emptySet());
	}

	private PathUpdate deletion(String layoutLocalPath) {
		return new PathUpdate(Collections.emptySet(), Collections.emptySet(),
			Collections.singleton(layoutPath(layoutLocalPath)));
	}

	private Path layoutPath(String layoutLocalPath) {
		return LAYOUTS.resolve(layoutLocalPath);
	}

}
