/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.HideTileArguments;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.Tile;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.TileContent;
import com.top_logic.layout.react.control.layout.TileWidth;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests replacing the tiles of a {@link ReactDashboardControl} at runtime with
 * {@link ReactDashboardControl#setTiles(List)}.
 */
public class TestReactDashboardTiles extends TestCase {

	/** State key holding the descriptors of the shown tiles. */
	private static final String CHILDREN = "children";

	/** Descriptor key holding a tile's id. */
	private static final String TILE_ID = "id";

	/** Descriptor key holding the control rendered inside a tile. */
	private static final String TILE_CONTROL = "control";

	/** Argument of the {@link ReactDashboardControl#REORDER_COMMAND} holding the new order. */
	private static final String ORDER_ARG = "order";

	/** Argument of the {@link ReactDashboardControl#HIDE_COMMAND} naming the tile. */
	private static final String HIDE_TILE_ARG = HideTileArguments.TILE_ID;

	/** Descriptor key holding a tile's width. */
	private static final String TILE_WIDTH = "width";

	/** Descriptor key holding a tile's row span. */
	private static final String TILE_ROW_SPAN = "rowSpan";

	/**
	 * A dashboard that hands out its own state, which is otherwise visible to the control only.
	 */
	private static final class DashboardProbe extends ReactDashboardControl {

		/** How often the tiles were published. */
		int _published;

		DashboardProbe(ReactContext context, List<Tile> tiles, List<List<String>> reorders,
				List<String> visibilityChanges) {
			super(context, "16rem", "16rem", tiles, reorders::add, (id, visible) -> visibilityChanges.add(id));
		}

		@Override
		protected void putState(String key, Object value) {
			if (CHILDREN.equals(key)) {
				_published++;
			}
			super.putState(key, value);
		}

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> descriptors() {
			return (List<Map<String, Object>>) getState(CHILDREN);
		}
	}

	private ReactContext _context;

	/** The content controls created per tile id, in creation order. */
	private Map<String, List<ReactControl>> _created;

	private List<List<String>> _reorders;

	private List<String> _visibilityChanges;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_created = new HashMap<>();
		_reorders = new ArrayList<>();
		_visibilityChanges = new ArrayList<>();
	}

	private Tile tile(String id) {
		return tile(id, TileWidth.QUARTER, 1, true);
	}

	private Tile tile(String id, TileWidth width, int rowSpan, boolean visible) {
		return new Tile(id, width, rowSpan, visible, () -> {
			ReactControl control = new ReactControl(_context, null, "TLText");
			_created.computeIfAbsent(id, x -> new ArrayList<>()).add(control);
			return new TileContent(control, null);
		});
	}

	private DashboardProbe dashboard(Tile... tiles) {
		DashboardProbe result = new DashboardProbe(_context, List.of(tiles), _reorders, _visibilityChanges);
		result.attach();
		return result;
	}

	/**
	 * A tile that stays keeps the control it displays, a new one gets a control of its own and
	 * appears where it is given, a dropped one leaves the state and the order and its control is
	 * disposed.
	 */
	public void testReplaceTiles() {
		DashboardProbe dashboard = dashboard(tile("a"), tile("b"), tile("c"));
		ReactControl controlA = onlyControl("a");
		ReactControl controlB = onlyControl("b");

		dashboard.setTiles(List.of(tile("a"), tile("c"), tile("d")));

		assertEquals(List.of("a", "c", "d"), ids(dashboard));
		assertSame("A tile that stays keeps its control.", controlA, control(dashboard, "a"));
		assertEquals("The content of a tile that stays is not created again.", 1, _created.get("a").size());
		assertTrue(controlB.isDisposed());
		assertFalse(controlA.isDisposed());
		assertSame(onlyControl("d"), control(dashboard, "d"));
		assertTrue("The content of a new tile is displayed.", onlyControl("d").isAttached());

		dashboard.executeClientCommand(ReactDashboardControl.REORDER_COMMAND, Map.of(ORDER_ARG, List.of("d", "a")));
		assertEquals("A dropped tile is no longer part of the stored order.",
			List.of(List.of("d", "c", "a")), _reorders);
	}

	/**
	 * A tile that stays takes over the width and row span it is given now.
	 */
	public void testTileTakesNewSize() {
		DashboardProbe dashboard = dashboard(tile("a"));
		ReactControl controlA = onlyControl("a");

		dashboard.setTiles(List.of(tile("a", TileWidth.HALF, 2, true)));

		Map<String, Object> descriptor = dashboard.descriptors().get(0);
		assertEquals(TileWidth.HALF.getExternalName(), descriptor.get(TILE_WIDTH));
		assertEquals(Integer.valueOf(2), descriptor.get(TILE_ROW_SPAN));
		assertSame(controlA, descriptor.get(TILE_CONTROL));
	}

	/**
	 * A tile that is now given hidden leaves the state, keeps its content detached, and shows the
	 * same content again when it is given shown later.
	 */
	public void testVisibilityOfStayingTile() {
		DashboardProbe dashboard = dashboard(tile("a"), tile("b"));
		ReactControl controlB = onlyControl("b");

		dashboard.setTiles(List.of(tile("a"), tile("b", TileWidth.QUARTER, 1, false)));
		assertEquals(List.of("a"), ids(dashboard));
		assertFalse(dashboard.isTileVisible("b"));
		assertFalse("A hidden tile's content is not displayed.", controlB.isAttached());
		assertFalse("A hidden tile's content is kept.", controlB.isDisposed());

		dashboard.setTiles(List.of(tile("a"), tile("b")));
		assertEquals(List.of("a", "b"), ids(dashboard));
		assertSame(controlB, control(dashboard, "b"));
		assertTrue(controlB.isAttached());
	}

	/**
	 * A new tile that is given hidden creates no content until it is shown.
	 */
	public void testHiddenNewTileCreatesNoContent() {
		DashboardProbe dashboard = dashboard(tile("a"));

		dashboard.setTiles(List.of(tile("a"), tile("x", TileWidth.QUARTER, 1, false)));

		assertNull(_created.get("x"));
		dashboard.showTile("x");
		assertSame(onlyControl("x"), control(dashboard, "x"));
	}

	/**
	 * Disposing the dashboard disposes the contents of the tiles given last, but not the ones
	 * disposed already.
	 */
	public void testCleanupAfterReplace() {
		DashboardProbe dashboard = dashboard(tile("a"));
		ReactControl controlA = onlyControl("a");
		dashboard.setTiles(List.of(tile("a"), tile("b")));
		ReactControl controlB = onlyControl("b");

		dashboard.cleanupTree();

		assertTrue(controlA.isDisposed());
		assertTrue(controlB.isDisposed());
	}

	/**
	 * A dashboard that may not be rearranged refuses a reorder and a hide: the tiles keep their
	 * order and visibility, no callback is told, and the unchanged tiles are published again.
	 */
	public void testRefusedRearrangement() {
		DashboardProbe dashboard = dashboard(tile("a"), tile("b"), tile("c"));
		dashboard.setRearrangeable(false);
		int published = dashboard._published;

		dashboard.executeClientCommand(ReactDashboardControl.REORDER_COMMAND, Map.of(ORDER_ARG, List.of("c", "b", "a")));
		assertEquals(List.of("a", "b", "c"), ids(dashboard));
		assertEquals(List.of(), _reorders);
		assertEquals("The unchanged tiles are published again.", published + 1, dashboard._published);

		dashboard.executeClientCommand(ReactDashboardControl.HIDE_COMMAND, Map.of(HIDE_TILE_ARG, "b"));
		assertTrue(dashboard.isTileVisible("b"));
		assertEquals(List.of("a", "b", "c"), ids(dashboard));
		assertEquals(List.of(), _visibilityChanges);
		assertEquals("The unchanged tiles are published again.", published + 2, dashboard._published);

		dashboard.setRearrangeable(true);
		dashboard.executeClientCommand(ReactDashboardControl.REORDER_COMMAND, Map.of(ORDER_ARG, List.of("c", "b", "a")));
		assertEquals(List.of("c", "b", "a"), ids(dashboard));
		dashboard.executeClientCommand(ReactDashboardControl.HIDE_COMMAND, Map.of(HIDE_TILE_ARG, "b"));
		assertEquals(List.of("b"), _visibilityChanges);
	}

	private ReactControl onlyControl(String id) {
		List<ReactControl> controls = _created.get(id);
		assertNotNull("No content created for tile '" + id + "'.", controls);
		assertEquals(1, controls.size());
		return controls.get(0);
	}

	private static List<String> ids(DashboardProbe dashboard) {
		List<String> result = new ArrayList<>();
		for (Map<String, Object> d : dashboard.descriptors()) {
			result.add((String) d.get(TILE_ID));
		}
		return result;
	}

	private static Object control(DashboardProbe dashboard, String id) {
		for (Map<String, Object> d : dashboard.descriptors()) {
			if (id.equals(d.get(TILE_ID))) {
				return d.get(TILE_CONTROL);
			}
		}
		fail("No tile '" + id + "' in the state.");
		return null;
	}
}
