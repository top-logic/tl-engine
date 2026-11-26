/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.view.command.ArgumentsProbe;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.layout.ActivateTileArguments;
import com.top_logic.layout.react.control.layout.HideTileArguments;
import com.top_logic.layout.react.control.layout.ItemAction;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.control.layout.TileWidth;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliques;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.DisabledIf;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Tests the tiles a {@link TileListElement &lt;tiles&gt;} entry contributes to a
 * {@link DashboardElement dashboard}: one tile per object, between the fixed tiles, sized by the
 * scripts over the object, following the computed objects, and activated with the object.
 */
public class TestDashboardComputedTiles extends BasicTestCase {

	/** Name of the view given inline. */
	private static final String INLINE_VIEW = "inline-dashboard-computed-tiles.view.xml";

	/** Id of the dashboard. */
	private static final String DASHBOARD = "computed";

	/** Name of the channel holding the objects the tiles are computed from. */
	private static final String NAMES = "names";

	/** Id of the {@code <tiles>} entry. */
	private static final String OBJECTS = "objects";

	/** Id of the fixed tile before the {@code <tiles>} entry. */
	private static final String FIRST = "first";

	/** Id of the fixed tile behind the {@code <tiles>} entry. */
	private static final String LAST = "last";

	/** The name the object is given by {@link TileListElement.Config#ELEMENT_AS}. */
	private static final String ELEMENT_AS = "name";

	/** The object whose tile is wide and two rows high. */
	private static final String BIG = "b";

	/** The object the action is disabled for. */
	private static final String LOCKED = "locked";

	/** The reason the action is disabled for {@link #LOCKED}. */
	private static final String LOCKED_REASON = "The object is locked.";

	/** The personal configuration key holding the tile order of the dashboard. */
	private static final String ORDER_KEY = "dashboard.tileOrder." + DASHBOARD;

	/** State key holding the descriptors of the shown tiles. */
	private static final String CHILDREN = "children";

	/** Descriptor key holding a tile's id. */
	private static final String TILE_ID = "id";

	/** Descriptor key holding a tile's width. */
	private static final String TILE_WIDTH = "width";

	/** Descriptor key holding a tile's row span. */
	private static final String TILE_ROW_SPAN = "rowSpan";

	/** Descriptor key holding a tile's name. */
	private static final String TILE_LABEL = "label";

	/** Descriptor key holding the control of a tile. */
	private static final String TILE_CONTROL = "control";

	/** Descriptor key holding the action of a tile. */
	private static final String TILE_ACTION = "action";

	private ViewContext _context;

	private ViewChannel _names;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ArgumentsProbe.reset();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_names = new DefaultViewChannel(NAMES);
		_context.registerChannel(NAMES, _names);
		_names.set(List.of("a", BIG));
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		_context = null;
		_names = null;

		super.tearDown();
	}

	/**
	 * The computed tiles stand at the place of their entry, in the order of the objects, with the
	 * ids, widths, row spans and names the scripts compute.
	 */
	public void testMixedOrderAndSizes() throws Exception {
		ReactDashboardControl dashboard = createDashboard(_context);
		dashboard.attach();

		assertEquals(List.of(FIRST, tileId("a"), tileId(BIG), LAST), tileIds(dashboard));

		Map<?, ?> a = tile(dashboard, tileId("a"));
		assertEquals(TileWidth.QUARTER.getExternalName(), a.get(TILE_WIDTH));
		assertEquals(1, ((Number) a.get(TILE_ROW_SPAN)).intValue());
		assertEquals("Tile a", a.get(TILE_LABEL));

		Map<?, ?> big = tile(dashboard, tileId(BIG));
		assertEquals(TileWidth.FULL.getExternalName(), big.get(TILE_WIDTH));
		assertEquals(2, ((Number) big.get(TILE_ROW_SPAN)).intValue());
	}

	/**
	 * A new object gets a tile, the tile of a vanished object disappears, and the tile of an object
	 * that stays keeps its control.
	 */
	public void testTilesFollowTheObjects() throws Exception {
		ReactDashboardControl dashboard = createDashboard(_context);
		dashboard.attach();
		Object bigControl = controlId(dashboard, tileId(BIG));

		_names.set(List.of(BIG, "c"));

		assertEquals(List.of(FIRST, tileId(BIG), tileId("c"), LAST), tileIds(dashboard));
		assertEquals("The tile of an object that stays keeps its control.", bigControl,
			controlId(dashboard, tileId(BIG)));
	}

	/**
	 * The stored order of the user comes first, stored ids without a tile are skipped, the other
	 * tiles follow in the order of their declaration, and a tile of a new object is placed behind
	 * the stored ones.
	 */
	public void testStoredOrder() throws Exception {
		inUserContext(() -> {
			PersonalConfiguration.getPersonalConfiguration().setJSONValue(ORDER_KEY,
				List.of(LAST, tileId(BIG), "gone"));

			ReactDashboardControl dashboard = createDashboard(_context);
			dashboard.attach();
			assertEquals(List.of(LAST, tileId(BIG), FIRST, tileId("a")), tileIds(dashboard));

			_names.set(List.of("a", BIG, "c"));
			assertEquals(List.of(LAST, tileId(BIG), FIRST, tileId("a"), tileId("c")), tileIds(dashboard));
		});
	}

	/**
	 * A computed tile is hidden like a fixed one, the hidden tile is offered to be added again
	 * under its name, and a command for the tile of a vanished object is withdrawn.
	 */
	public void testHideComputedTile() throws Exception {
		inUserContext(() -> {
			CommandScope scope = new CommandScope(List.of());
			ReactDashboardControl dashboard = createDashboard(_context.withScope(CommandScope.class, scope));
			dashboard.attach();
			dashboard.enterEditMode();

			assertNull(CommandFailures.outcome(() -> dashboard.executeClientCommand(
				ReactDashboardControl.HIDE_COMMAND, Map.of(HideTileArguments.TILE_ID, tileId("a")))));
			assertEquals(List.of(FIRST, tileId(BIG), LAST), tileIds(dashboard));
			assertEquals(Map.of(tileId("a"), Boolean.FALSE), PersonalConfiguration.getPersonalConfiguration()
				.getJSONValue("dashboard.tileVisibility." + DASHBOARD));

			CommandModel add = tileCommand(scope, tileId("a"));
			assertNotNull(add);
			assertTrue("The hidden tile is offered to be added.", add.isVisible());
			assertEquals("Tile a", add.getLabel());

			_names.set(List.of(BIG));
			assertNull("The command of a vanished tile is withdrawn.", tileCommand(scope, tileId("a")));
		});
	}

	/**
	 * Activating a computed tile runs the action with its object, named by
	 * {@link TileListElement.Config#ELEMENT_AS}; an object the action is disabled for shows it and
	 * activating its tile runs nothing.
	 */
	public void testActivation() throws Exception {
		_names.set(List.of("a", LOCKED));
		ReactDashboardControl dashboard = createDashboard(_context);
		dashboard.attach();

		Map<?, ?> action = (Map<?, ?>) tile(dashboard, tileId("a")).get(TILE_ACTION);
		assertEquals(Boolean.FALSE, action.get(ItemAction.DISABLED));
		Map<?, ?> locked = (Map<?, ?>) tile(dashboard, tileId(LOCKED)).get(TILE_ACTION);
		assertEquals(Boolean.TRUE, locked.get(ItemAction.DISABLED));
		assertEquals(LOCKED_REASON, locked.get(ItemAction.TOOLTIP));
		assertNull("A fixed tile without action is no entry point.", tile(dashboard, FIRST).get(TILE_ACTION));

		activate(dashboard, LOCKED);
		assertEquals("The action does not run for a refused object.", List.of(), ArgumentsProbe.SEEN);

		activate(dashboard, "a");
		assertEquals(1, ArgumentsProbe.SEEN.size());
		assertEquals("a", ArgumentsProbe.SEEN.get(0).value());
		assertEquals("a", ArgumentsProbe.SEEN.get(0).get(ELEMENT_AS));
	}

	private static void activate(ReactDashboardControl dashboard, String name) {
		assertNull(CommandFailures.outcome(() -> dashboard.executeClientCommand(
			ReactDashboardControl.ACTIVATE_COMMAND, Map.of(ActivateTileArguments.TILE_ID, tileId(name)))));
	}

	private static String tileId(String name) {
		return OBJECTS + TileListElement.TILE_ID_SEPARATOR + name;
	}

	private static CommandModel tileCommand(CommandScope scope, String tileId) {
		for (CommandModel command : scope.getAllCommands()) {
			if (CommandCliques.DASHBOARD_TILES.equals(command.getClique())
				&& (DashboardCommandModel.SHOW_TILE_COMMAND_PREFIX + tileId).equals(command.getName())) {
				return command;
			}
		}
		return null;
	}

	private static void inUserContext(Runnable test) {
		TLContextManager.inPersonContext(PersonManager.getManager().getRoot(), () -> {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			pc.setJSONValue(ORDER_KEY, null);
			pc.setJSONValue("dashboard.tileVisibility." + DASHBOARD, null);
			test.run();
		});
	}

	private static List<String> tileIds(ReactDashboardControl dashboard) {
		List<String> result = new ArrayList<>();
		for (Object child : children(dashboard)) {
			result.add((String) ((Map<?, ?>) child).get(TILE_ID));
		}
		return result;
	}

	private static Object controlId(ReactDashboardControl dashboard, String tileId) {
		return ((Map<?, ?>) tile(dashboard, tileId).get(TILE_CONTROL)).get(ChildControl.CONTROL_ID__PROP);
	}

	private static Map<?, ?> tile(ReactDashboardControl dashboard, String tileId) {
		for (Object child : children(dashboard)) {
			Map<?, ?> tile = (Map<?, ?>) child;
			if (tileId.equals(tile.get(TILE_ID))) {
				return tile;
			}
		}
		throw new AssertionError("No tile '" + tileId + "' in the dashboard.");
	}

	private static List<?> children(ReactDashboardControl dashboard) {
		String state = dashboard.stateAsJSON();
		try {
			return (List<?>) ((Map<?, ?>) JSON.fromString(state)).get(CHILDREN);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a dashboard: " + state, ex);
		}
	}

	private static ReactDashboardControl createDashboard(ViewContext context) {
		String view = """
			<view>
				<dashboard id="%s">
					<tile id="%s" width="half">
						<card/>
					</tile>
					<tiles id="%s"
						inputs="%s"
						items="$%s"
						element-as="%s"
						width='if($%s == "%s", "full", "quarter")'
						row-span='if($%s == "%s", 2, 1)'
						label='toString("Tile ", $%s)'
					>
						<text input="element"/>
						<action class="%s">
							<executability>
								<%s expr='if($%s == "%s", "%s")'/>
							</executability>
							<action class="%s"/>
						</action>
					</tiles>
					<tile id="%s" width="half">
						<card/>
					</tile>
				</dashboard>
			</view>
			""".formatted(DASHBOARD, FIRST, OBJECTS, NAMES, NAMES, ELEMENT_AS,
			ELEMENT_AS, BIG, ELEMENT_AS, BIG, ELEMENT_AS,
			GenericViewCommand.class.getName(),
			DisabledIf.Config.TAG_NAME, ELEMENT_AS, LOCKED, LOCKED_REASON,
			ArgumentsProbe.class.getName(), LAST);
		try {
			ViewElement.Config config =
				ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, INLINE_VIEW)));
			DefaultInstantiationContext instantiation =
				new DefaultInstantiationContext(TestDashboardComputedTiles.class);
			DashboardElement element = (DashboardElement) instantiation.getInstance(config.getContent());
			instantiation.checkErrors();
			return (ReactDashboardControl) element.createControl(context);
		} catch (ConfigurationException ex) {
			throw new AssertionError("The dashboard view does not parse.", ex);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The objects, the sizes and the rules are TL-Script expressions, which need the
	 *           application model and the {@link com.top_logic.knowledge.service.KnowledgeBase};
	 *           whether the layout may be edited depends on the account logged in, which the
	 *           {@link PersonManager} provides.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestDashboardComputedTiles.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE, PersonManager.Module.INSTANCE));
	}

}
