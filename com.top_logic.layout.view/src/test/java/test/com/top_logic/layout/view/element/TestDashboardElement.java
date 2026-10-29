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
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.layout.ActivateTileArguments;
import com.top_logic.layout.react.control.layout.HideTileArguments;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliques;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.DashboardCommandModel;
import com.top_logic.layout.view.element.DashboardElement;
import com.top_logic.layout.view.element.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Tests the {@code <tile>}s of a {@code <dashboard>}: the action a tile is activated by - what the
 * dashboard tells the client about it, and what activating a tile runs - and hiding a tile and
 * adding it again.
 *
 * <p>
 * Everything is observed through the seams the client and the user see - the tile descriptors the
 * dashboard publishes, the commands it answers, the commands it contributes to the surrounding
 * toolbar, and what it stores in the personal configuration - so that what is tested is the
 * contract the dashboard is rendered and personalized from.
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

	/** Id of the tile that only displays its content. */
	private static final String DISPLAY_TILE = "display";

	/** Id of the tile whose action runs over the {@link #INPUT_CHANNEL}. */
	private static final String OPENING_TILE = "opening";

	/** Id of the tile whose action carries a label of its own. */
	private static final String LABELED_TILE = "labeled";

	/** Id of the tile whose content has no title to name its action after. */
	private static final String UNTITLED_TILE = "untitled";

	/** Id of the optional tile, hidden until the user adds it. */
	private static final String OPTIONAL_TILE = "extra";

	/** The name the {@link #OPTIONAL_TILE} is configured with. */
	private static final String OPTIONAL_TILE_LABEL = "Extras";

	/** Id of the dashboard in {@code test-dashboard.view.xml}. */
	private static final String DASHBOARD_ID = "test-dashboard";

	/** The personal configuration key holding the tile visibilities of the test dashboard. */
	private static final String VISIBILITY_KEY = DashboardElement.PC_VISIBILITY_KEY_PREFIX + DASHBOARD_ID;

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
	 * nothing, and the activation answers with the refusal, which says why.
	 */
	public void testActivateWhileRefusedRunsNothing() {
		ReactDashboardControl dashboard = createDashboard();
		dashboard.attach();

		HandlerResult result = activate(dashboard, OPENING_TILE);

		assertEquals("The command of a refused tile does not run.", List.of(), OpeningCommand.OPENED);
		assertFalse("The refusal is reported.", result.isSuccess());
		assertEquals("A refused activation is no malfunction.", ErrorSeverity.WARNING, result.getErrorSeverity());
		assertEquals(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE,
			result.getErrorTitle());
		assertNotNull("The refusal says why.", result.getErrorMessage());
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

	/**
	 * Tests that a logged-in user is offered to rearrange the dashboard, and the anonymous session
	 * is not: all anonymous visitors share one account, which has no personal tile order to store.
	 */
	public void testLayoutEditingNeedsAnAccountOfItsOwn() {
		assertTrue("Precondition: the test runs without a logged-in user.", TLContext.isAnonymous());
		assertEquals("The anonymous session is offered no layout editing.", List.of(),
			layoutCommands(new CommandScope(List.of())));

		List<String> offered = new ArrayList<>();
		TLContextManager.inPersonContext(PersonManager.getManager().getRoot(), () -> {
			for (CommandModel command : layoutCommands(new CommandScope(List.of()))) {
				offered.add(command.getName());
			}
		});
		assertTrue("A logged-in user is offered to edit the layout.",
			offered.contains(DashboardCommandModel.EDIT_COMMAND));
		assertTrue("A logged-in user is offered to finish editing.",
			offered.contains(DashboardCommandModel.DONE_COMMAND));
	}

	/**
	 * Tests that the anonymous session can neither hide a tile nor add one: it is offered no
	 * command to add a tile, and a hide command sent anyway leaves the tile in place.
	 */
	public void testAnonymousSessionCannotHideOrAddTiles() {
		assertTrue("Precondition: the test runs without a logged-in user.", TLContext.isAnonymous());
		CommandScope scope = new CommandScope(List.of());
		ReactDashboardControl dashboard = createDashboard(_context.withScope(CommandScope.class, scope));
		dashboard.attach();

		assertEquals("The anonymous session is offered no tile to add.", List.of(), tileCommands(scope));
		assertFalse("An optional tile stays hidden for the anonymous session.",
			tileIds(dashboard).contains(OPTIONAL_TILE));

		hide(dashboard, DISPLAY_TILE);
		assertTrue("The anonymous session cannot hide a tile.", tileIds(dashboard).contains(DISPLAY_TILE));
	}

	/**
	 * Tests that hiding a tile drops it from what the client is told and stores the deviation from
	 * the default in the personal configuration.
	 */
	public void testHideTile() {
		inUserContext(() -> {
			CommandScope scope = new CommandScope(List.of());
			ReactDashboardControl dashboard = createDashboard(_context.withScope(CommandScope.class, scope));
			dashboard.attach();
			dashboard.enterEditMode();

			hide(dashboard, DISPLAY_TILE);

			assertEquals("The hidden tile is no longer published, the others keep their order.",
				List.of(OPENING_TILE, LABELED_TILE, UNTITLED_TILE), tileIds(dashboard));
			assertEquals("Only the deviation from the default is stored.",
				Map.of(DISPLAY_TILE, Boolean.FALSE), storedVisibility());

			ReactDashboardControl reopened = createDashboard(_context.withScope(CommandScope.class,
				new CommandScope(List.of())));
			assertFalse("A tile hidden once stays hidden when the dashboard is displayed again.",
				tileIds(reopened).contains(DISPLAY_TILE));
		});
	}

	/**
	 * Tests the command adding a hidden tile again: offered only while the layout is edited and the
	 * tile is hidden, and adding the tile shows it at its former position.
	 */
	public void testAddHiddenTileAgain() {
		inUserContext(() -> {
			CommandScope scope = new CommandScope(List.of());
			ReactDashboardControl dashboard = createDashboard(_context.withScope(CommandScope.class, scope));
			dashboard.attach();

			CommandModel add = tileCommand(scope, DISPLAY_TILE);
			assertEquals("The command is grouped with the other tiles to add.", CommandCliques.DASHBOARD_TILES,
				add.getClique());
			assertEquals("The command is named after the tile.", "Revenue", add.getLabel());
			assertFalse("A shown tile is not offered to be added.", add.isVisible());

			dashboard.enterEditMode();
			assertFalse("A shown tile is not offered to be added, also while editing.", add.isVisible());

			hide(dashboard, DISPLAY_TILE);
			assertTrue("A hidden tile is offered to be added while editing.", add.isVisible());

			dashboard.exitEditMode();
			assertFalse("A tile is offered to be added only while editing.", add.isVisible());

			dashboard.enterEditMode();
			assertTrue(add.executeCommand(_context).isSuccess());

			assertEquals("The tile is shown again at its former position.",
				List.of(DISPLAY_TILE, OPENING_TILE, LABELED_TILE, UNTITLED_TILE), tileIds(dashboard));
			assertFalse("A tile shown again is no longer offered to be added.", add.isVisible());
			assertNull("A tile back at its default leaves nothing stored.",
				PersonalConfiguration.getPersonalConfiguration().getJSONValue(VISIBILITY_KEY));
		});
	}

	/**
	 * Tests that the action of a tile follows its input again once a hidden tile is shown again.
	 */
	public void testActionFollowsItsInputAfterAddingAgain() {
		inUserContext(() -> {
			CommandScope scope = new CommandScope(List.of());
			ReactDashboardControl dashboard = createDashboard(_context.withScope(CommandScope.class, scope));
			dashboard.attach();
			dashboard.enterEditMode();

			hide(dashboard, OPENING_TILE);
			_input.set(TICKET);
			tileCommand(scope, OPENING_TILE).executeCommand(_context);

			assertEquals("The action of the tile shown again is offered for the selected input.", Boolean.FALSE,
				action(dashboard, OPENING_TILE).get(ACTION_DISABLED));
			_input.set(null);
			assertEquals("The action of the tile shown again follows its input.", Boolean.TRUE,
				action(dashboard, OPENING_TILE).get(ACTION_DISABLED));
		});
	}

	/**
	 * Tests that an optional tile starts hidden, is offered under its configured name, and is
	 * stored as shown once the user adds it.
	 */
	public void testOptionalTile() {
		inUserContext(() -> {
			CommandScope scope = new CommandScope(List.of());
			ReactDashboardControl dashboard = createDashboard(_context.withScope(CommandScope.class, scope));
			dashboard.attach();

			assertFalse("An optional tile is hidden by default.", tileIds(dashboard).contains(OPTIONAL_TILE));

			dashboard.enterEditMode();
			CommandModel add = tileCommand(scope, OPTIONAL_TILE);
			assertTrue("An optional tile is offered to be added while editing.", add.isVisible());
			assertEquals("The command is named by the label of the tile.", OPTIONAL_TILE_LABEL, add.getLabel());

			add.executeCommand(_context);

			assertEquals("The added tile is shown at its configured position.",
				List.of(DISPLAY_TILE, OPENING_TILE, LABELED_TILE, UNTITLED_TILE, OPTIONAL_TILE), tileIds(dashboard));
			assertEquals("Showing an optional tile is a deviation from its default.",
				Map.of(OPTIONAL_TILE, Boolean.TRUE), storedVisibility());
		});
	}

	/**
	 * Tests that stored visibilities of tiles the dashboard no longer has are ignored, and that a
	 * tile without a stored visibility - e.g. one configured after the user personalized the
	 * dashboard - is shown.
	 */
	public void testStoredVisibilityOfUnknownAndNewTiles() {
		inUserContext(() -> {
			PersonalConfiguration.getPersonalConfiguration().setJSONValue(VISIBILITY_KEY,
				Map.of("removed", Boolean.TRUE, OPENING_TILE, Boolean.FALSE));

			ReactDashboardControl dashboard = createDashboard(_context.withScope(CommandScope.class,
				new CommandScope(List.of())));

			assertEquals("A tile without a stored visibility is shown, a stored unknown tile is ignored.",
				List.of(DISPLAY_TILE, LABELED_TILE, UNTITLED_TILE), tileIds(dashboard));
		});
	}

	/**
	 * The layout commands a dashboard placed in a command scope contributes to it once displayed.
	 */
	private List<CommandModel> layoutCommands(CommandScope scope) {
		ReactDashboardControl dashboard = createDashboard(_context.withScope(CommandScope.class, scope));
		dashboard.attach();
		List<CommandModel> result = new ArrayList<>();
		for (CommandModel command : scope.getAllCommands()) {
			if (command instanceof DashboardCommandModel) {
				result.add(command);
			}
		}
		return result;
	}

	/**
	 * Runs the given test code for a logged-in user whose dashboard is not yet personalized.
	 */
	private static void inUserContext(Runnable test) {
		TLContextManager.inPersonContext(PersonManager.getManager().getRoot(), () -> {
			PersonalConfiguration.getPersonalConfiguration().setJSONValue(VISIBILITY_KEY, null);
			test.run();
		});
	}

	/** The tile visibilities stored in the personal configuration. */
	private static Map<?, ?> storedVisibility() {
		Object value = PersonalConfiguration.getPersonalConfiguration().getJSONValue(VISIBILITY_KEY);
		assertTrue("Tile visibilities are stored as JSON object: " + value, value instanceof Map);
		return (Map<?, ?>) value;
	}

	/** The commands adding a tile that a dashboard contributed to the given scope. */
	private static List<CommandModel> tileCommands(CommandScope scope) {
		List<CommandModel> result = new ArrayList<>();
		for (CommandModel command : scope.getAllCommands()) {
			if (CommandCliques.DASHBOARD_TILES.equals(command.getClique())) {
				result.add(command);
			}
		}
		return result;
	}

	/** The command adding the tile with the given id. */
	private static CommandModel tileCommand(CommandScope scope, String tileId) {
		for (CommandModel command : tileCommands(scope)) {
			if ((DashboardCommandModel.SHOW_TILE_COMMAND_PREFIX + tileId).equals(command.getName())) {
				return command;
			}
		}
		throw new AssertionError("No command to add tile '" + tileId + "'.");
	}

	/** Sends the client's hide command for the tile with the given id. */
	private static void hide(ReactDashboardControl dashboard, String tileId) {
		HandlerResult result = dashboard.executeClientCommand(ReactDashboardControl.HIDE_COMMAND,
			Map.of(HideTileArguments.TILE_ID, tileId));
		assertTrue("Hiding a tile succeeds.", result.isSuccess());
	}

	/** The ids of the tiles the dashboard currently publishes, in their order. */
	private static List<String> tileIds(ReactDashboardControl dashboard) {
		List<String> result = new ArrayList<>();
		for (Object child : children(dashboard)) {
			result.add((String) ((Map<?, ?>) child).get(TILE_ID));
		}
		return result;
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
		return createDashboard(_context);
	}

	/** The dashboard of {@code test-dashboard.view.xml}, built in the given context. */
	private static ReactDashboardControl createDashboard(ViewContext context) {
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

		return (ReactDashboardControl) element.createControl(context);
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The command models of the tiles observe the objects their input points to, which
	 *           needs the {@link com.top_logic.knowledge.service.KnowledgeBase} those objects live
	 *           in; the icon marking a tile as an entry point is taken from the
	 *           {@link ThemeFactory}; whether the layout may be edited depends on the account
	 *           logged in, which the {@link PersonManager} provides.
	 *
	 * @see com.top_logic.layout.view.element.TileElement.Config#getAction()
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestDashboardElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ThemeFactory.Module.INSTANCE, ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE));
	}

}
