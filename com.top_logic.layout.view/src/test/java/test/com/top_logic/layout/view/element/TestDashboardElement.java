/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.layout.ActivateTileArguments;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.DashboardElement;
import com.top_logic.layout.view.element.I18NConstants;
import com.top_logic.layout.view.element.Icons;
import com.top_logic.layout.view.element.TileElement;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Tests the action a {@code <tile>} of a {@code <dashboard>} is activated by: what the dashboard
 * tells the client about it, and what activating a tile runs.
 *
 * <p>
 * Everything is observed through the seam the client sees - the tile descriptors the dashboard
 * publishes and the {@code activate} command it answers - so that what is tested is the contract
 * the tile is rendered from.
 * </p>
 */
public class TestDashboardElement extends BasicTestCase {

	/** Name of the channel the tile actions take their input from. */
	private static final String INPUT_CHANNEL = "ch";

	/** The value the input channel carries while the action is offered. */
	private static final String TICKET = "ticket";

	/** Descriptor key holding the tiles of the dashboard. */
	private static final String CHILDREN = "children";

	/** Descriptor key holding a tile's id. */
	private static final String TILE_ID = "id";

	/** Descriptor key holding a tile's action, absent for a tile that only displays content. */
	private static final String TILE_ACTION = "action";

	/** Action descriptor key holding the name the action is offered under. */
	private static final String ACTION_LABEL = "label";

	/** Action descriptor key telling whether the action is currently refused. */
	private static final String ACTION_DISABLED = "disabled";

	/** Action descriptor key holding the text explaining the action. */
	private static final String ACTION_TOOLTIP = "tooltip";

	/** Action descriptor key holding the icon marking the tile as an entry point. */
	private static final String ACTION_IMAGE = "image";

	/** Id of the tile that only displays its content. */
	private static final String DISPLAY_TILE = "display";

	/** Id of the tile whose action runs over the {@link #INPUT_CHANNEL}. */
	private static final String OPENING_TILE = "opening";

	/** Id of the tile whose action carries a label of its own. */
	private static final String LABELED_TILE = "labeled";

	/** Id of the tile whose content has no title to name its action after. */
	private static final String UNTITLED_TILE = "untitled";

	/**
	 * The command the tiles of {@code test-dashboard.view.xml} are activated by.
	 *
	 * <p>
	 * It records what it was run with, so that a test can tell an activation that ran from one that
	 * was refused. The record is static because the configuration builds the command, not the test.
	 * </p>
	 */
	public static class OpeningCommand implements ViewCommand {

		/** What the command was executed with, one entry per run. */
		static final List<Object> OPENED = new ArrayList<>();

		/**
		 * Configuration for {@link OpeningCommand}.
		 */
		public interface Config extends ViewCommand.Config {

			@Override
			@ClassDefault(OpeningCommand.class)
			Class<? extends ViewCommand> getImplementationClass();
		}

		/**
		 * Creates a new {@link OpeningCommand}.
		 */
		@CalledByReflection
		public OpeningCommand(InstantiationContext context, Config config) {
			// No configuration.
		}

		@Override
		public HandlerResult execute(ReactContext context, Object input) {
			OPENED.add(input);
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	private ViewChannel _input;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		OpeningCommand.OPENED.clear();
		_input = new DefaultViewChannel(INPUT_CHANNEL);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(INPUT_CHANNEL, _input);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_input = null;
		OpeningCommand.OPENED.clear();

		super.tearDown();
	}

	/**
	 * Tests that a tile configuring no action stays a display: nothing tells the client it could be
	 * activated.
	 */
	public void testTileWithoutActionOffersNothing() {
		ReactDashboardControl dashboard = createDashboard();
		dashboard.attach();

		assertNull("A tile without an action is not an entry point.", action(dashboard, DISPLAY_TILE));
	}

	/**
	 * Tests that the tile follows what its command says about the channel it takes its input from:
	 * refused while there is no input - with the reason to show for it - and offered as soon as
	 * there is one.
	 */
	public void testActionFollowsItsInput() {
		ReactDashboardControl dashboard = createDashboard();
		dashboard.attach();

		Map<String, Object> refused = action(dashboard, OPENING_TILE);
		assertNotNull("A tile with an action is an entry point, refused or not.", refused);
		assertEquals("Nothing is selected, so there is nothing to open.",
			Boolean.TRUE, refused.get(ACTION_DISABLED));
		assertNotNull("A refused action says why.", refused.get(ACTION_TOOLTIP));
		assertFalse("A refused action says why.", refused.get(ACTION_TOOLTIP).toString().isEmpty());

		_input.set(TICKET);

		Map<String, Object> offered = action(dashboard, OPENING_TILE);
		assertEquals("The action is offered for the selected input.", Boolean.FALSE,
			offered.get(ACTION_DISABLED));
		assertEquals("A command without an image of its own marks the tile with the entry-point icon.",
			Icons.TILE_ACTIVATE.resolve().toEncodedForm(), offered.get(ACTION_IMAGE));
	}

	/**
	 * Tests that activating the tile runs its command with what the tile's input channel holds.
	 */
	public void testActivateRunsTheCommand() {
		ReactDashboardControl dashboard = createDashboard();
		dashboard.attach();
		_input.set(TICKET);

		HandlerResult result = activate(dashboard, OPENING_TILE);

		assertTrue("Activating the tile succeeds.", result.isSuccess());
		assertEquals("The command runs with the value of the tile's input channel.",
			List.of(TICKET), OpeningCommand.OPENED);
	}

	/**
	 * Tests that a refused tile stays refused whatever the client sends: activating it runs
	 * nothing.
	 */
	public void testActivateWhileRefusedRunsNothing() {
		ReactDashboardControl dashboard = createDashboard();
		dashboard.attach();

		HandlerResult result = activate(dashboard, OPENING_TILE);

		assertTrue("A refused activation is no error.", result.isSuccess());
		assertEquals("The command of a refused tile does not run.", List.of(), OpeningCommand.OPENED);
	}

	/**
	 * Tests the name the tile is offered under: the label of its command, or - where the command
	 * carries none - the title of what the tile shows.
	 */
	public void testActionName() {
		ReactDashboardControl dashboard = createDashboard();
		dashboard.attach();

		Resources resources = Resources.getInstance();
		assertEquals("A command with a label of its own is offered under it.",
			resources.getString(ResKey.text("Details")), action(dashboard, LABELED_TILE).get(ACTION_LABEL));
		assertEquals("A command without a label is named after the title of the tile's content.",
			resources.getString(I18NConstants.TILE_ACTIVATE__TITLE.fill(ResKey.text("Tickets"))),
			action(dashboard, OPENING_TILE).get(ACTION_LABEL));
		assertEquals("Content without a title leaves the plain name of opening something.",
			resources.getString(I18NConstants.TABLE_ACTIVATE_ROW),
			action(dashboard, UNTITLED_TILE).get(ACTION_LABEL));
	}

	/** Sends the client's activate command for the tile with the given id. */
	private static HandlerResult activate(ReactDashboardControl dashboard, String tileId) {
		return dashboard.executeClientCommand(ReactDashboardControl.ACTIVATE_COMMAND,
			Map.of(ActivateTileArguments.TILE_ID, tileId));
	}

	/**
	 * What the client is told about the action of the tile with the given id, {@code null} for a
	 * tile that is not an entry point.
	 */
	private static Map<String, Object> action(ReactDashboardControl dashboard, String tileId) {
		for (Object child : children(dashboard)) {
			Map<?, ?> tile = (Map<?, ?>) child;
			if (tileId.equals(tile.get(TILE_ID))) {
				@SuppressWarnings("unchecked")
				Map<String, Object> action = (Map<String, Object>) tile.get(TILE_ACTION);
				return action;
			}
		}
		throw new AssertionError("No tile '" + tileId + "' in the dashboard.");
	}

	/** The tile descriptors the dashboard currently publishes, as the client receives them. */
	private static List<?> children(ReactDashboardControl dashboard) {
		String state = dashboard.stateAsJSON();
		try {
			return (List<?>) ((Map<?, ?>) JSON.fromString(state)).get(CHILDREN);
		} catch (Exception ex) {
			throw new AssertionError("Not the JSON state of a dashboard: " + state, ex);
		}
	}

	/** The dashboard of {@code test-dashboard.view.xml}, built in the test context. */
	private ReactDashboardControl createDashboard() {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestDashboardElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		BinaryContent source = new ClassRelativeBinaryContent(TestDashboardElement.class, "test-dashboard.view.xml");

		ViewElement.Config config;
		try {
			ConfigurationReader reader = new ConfigurationReader(instantiation, descriptors);
			reader.setSource(source);
			config = (ViewElement.Config) reader.read();
			instantiation.checkErrors();
		} catch (Exception ex) {
			throw new AssertionError("The dashboard view does not parse.", ex);
		}

		assertTrue("The content of the view is a <dashboard>.", config.getContent() instanceof DashboardElement.Config);
		UIElement element = instantiation.getInstance(config.getContent());
		assertTrue("The configuration builds a dashboard.", element instanceof DashboardElement);

		return (ReactDashboardControl) element.createControl(_context);
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The command models of the tiles observe the objects their input points to, which
	 *           needs the {@link com.top_logic.knowledge.service.KnowledgeBase} those objects live
	 *           in; the icon marking a tile as an entry point is taken from the
	 *           {@link ThemeFactory}.
	 *
	 * @see TileElement.Config#getAction()
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestDashboardElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ThemeFactory.Module.INSTANCE, ModelService.Module.INSTANCE));
	}

}
