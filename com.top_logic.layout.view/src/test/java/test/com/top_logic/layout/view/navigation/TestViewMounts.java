/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.navigation;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.element.AdaptiveDetailElement;
import com.top_logic.layout.view.element.SidebarElement;
import com.top_logic.layout.view.element.TabBarElement;
import com.top_logic.layout.view.navigation.MountPath;
import com.top_logic.layout.view.navigation.MountStep;
import com.top_logic.layout.view.navigation.ViewMounts;
import com.top_logic.layout.view.tiles.TileStackElement;

/**
 * Tests for {@link ViewMounts} scanning the view files reachable from a root view.
 */
public class TestViewMounts extends TestCase {

	private static final String ROOT = "mounts-root.view.xml";

	private static final String SHARED = "mounts-shared.view.xml";

	private static final String TILE = "mounts-tile.view.xml";

	private static final String ORPHAN = "mounts-orphan.view.xml";

	private static final String CYCLE_A = "mounts-cycle-a.view.xml";

	private static final String CYCLE_B = "mounts-cycle-b.view.xml";

	private ViewMounts _mounts;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_mounts = ViewMounts.scan(ROOT, new FixtureViews());
	}

	/**
	 * The root view is mounted, at no container in particular.
	 */
	public void testRootIsMountedWithoutSteps() {
		assertEquals(ROOT, _mounts.getRootView());
		assertTrue(_mounts.isMounted(ROOT));

		List<MountPath> paths = _mounts.getMounts(ROOT);
		assertEquals(1, paths.size());
		assertEquals(List.of(), paths.get(0).steps());
	}

	/**
	 * A view referenced from two places has two mount paths, each naming the containers to open on
	 * the way - and only those that choose between their content.
	 */
	public void testDoublyReferencedViewHasTwoMounts() {
		List<MountPath> paths = _mounts.getMounts(SHARED);
		assertEquals("Referenced from a tab and from a master-detail selector.", 2, paths.size());

		MountPath viaTab = paths.get(0);
		assertEquals(SHARED, viaTab.viewRef());
		assertEquals(2, viaTab.steps().size());
		assertStep(viaTab.steps().get(0), SidebarElement.class, "home");
		assertStep(viaTab.steps().get(1), TabBarElement.class, "overview");

		MountPath viaSelector = paths.get(1);
		assertEquals(2, viaSelector.steps().size());
		assertStep(viaSelector.steps().get(0), SidebarElement.class, "admin");
		assertStep(viaSelector.steps().get(1), AdaptiveDetailElement.class, AdaptiveDetailElement.Config.SELECTOR);

		assertSame("Both mounts address the one sidebar of the root view.",
			viaTab.steps().get(0).container(), viaSelector.steps().get(0).container());
	}

	/**
	 * The initial view of a tile stack is mounted under the stack; the app shell on the way
	 * contributes no step, because it displays all of its content at once.
	 */
	public void testTileStackInitialViewIsMounted() {
		List<MountPath> paths = _mounts.getMounts(TILE);
		assertEquals(1, paths.size());

		List<MountStep> steps = paths.get(0).steps();
		assertEquals(3, steps.size());
		assertStep(steps.get(0), SidebarElement.class, "home");
		assertStep(steps.get(1), TabBarElement.class, "drilldown");
		assertStep(steps.get(2), TileStackElement.class, TileStackElement.Config.INITIAL);
	}

	/**
	 * A view file no reference reaches is not mounted.
	 */
	public void testUnreferencedViewIsNotMounted() {
		assertFalse(_mounts.isMounted(ORPHAN));
		assertEquals(List.of(), _mounts.getMounts(ORPHAN));
	}

	/**
	 * The scan answers the channels a mounted view declares, and nothing for a view it does not
	 * reach.
	 */
	public void testChannelsOfMountedViews() {
		assertEquals(List.of("item", "filter"), List.copyOf(_mounts.getChannelNames(SHARED)));
		assertEquals(List.of("selection"), List.copyOf(_mounts.getChannelNames(ROOT)));
		assertEquals(List.of(), List.copyOf(_mounts.getChannelNames(ORPHAN)));
	}

	/**
	 * Every view reached is reported, including the ones without channels of their own.
	 */
	public void testMountedViews() {
		assertEquals(List.of(ROOT, SHARED, TILE), List.copyOf(_mounts.getMountedViews()));
	}

	/**
	 * A view reaching itself is mounted where its references put it, and the scan terminates.
	 */
	public void testReferenceCycle() {
		ViewMounts cyclic = ViewMounts.scan(CYCLE_A, new FixtureViews());

		assertEquals(List.of(CYCLE_A, CYCLE_B), List.copyOf(cyclic.getMountedViews()));
		assertEquals("Mounted as the root, and again below the view it references.",
			2, cyclic.getMounts(CYCLE_A).size());
		assertEquals(1, cyclic.getMounts(CYCLE_B).size());
	}

	/**
	 * A full path names the same view as the reference written in the configuration.
	 */
	public void testFullPathsAreAccepted() {
		assertTrue(_mounts.isMounted("/WEB-INF/views/" + SHARED));
		assertEquals(_mounts.getMounts(SHARED), _mounts.getMounts("/WEB-INF/views/" + SHARED));
	}

	private static void assertStep(MountStep step, Class<?> containerType, String key) {
		assertEquals("Container of " + step, containerType, step.container().getClass());
		assertEquals("Key of " + step, key, step.key());
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestViewMounts.class, TypeIndex.Module.INSTANCE);
	}
}
