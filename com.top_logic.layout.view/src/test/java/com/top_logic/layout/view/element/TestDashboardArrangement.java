/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.layout.HideTileArguments;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.TLObject;

/**
 * Tests a {@link DashboardElement dashboard} whose arrangement is stored by the application
 * ({@link DashboardElement.Config#getArrangement() arrangement},
 * {@link DashboardElement.Config#getStoreArrangement() store-arrangement},
 * {@link DashboardElement.Config#getArrangementAccess() arrangement-access}), and one whose personal
 * arrangement is stored per displayed object
 * ({@link DashboardElement.Config#getPersonalizeBy() personalize-by}).
 */
public class TestDashboardArrangement extends AbstractModelAccessTest {

	/** Name of the view given inline. */
	private static final String INLINE_VIEW = "inline-dashboard-arrangement.view.xml";

	/** Id of the dashboard. */
	private static final String DASHBOARD = "arranged";

	/** The input of the dashboard, holding the displayed project. */
	private static final String PROJECT_INPUT = "project";

	/** The project attribute holding the stored tile order, comma separated. */
	private static final String ORDER_ATTRIBUTE = "place";

	/** The project attribute holding the ids of the hidden tiles. */
	private static final String HIDDEN_ATTRIBUTE = "keywords";

	private static final String FIRST = "first";

	private static final String SECOND = "second";

	private static final String LAST = "last";

	/** State key holding the descriptors of the shown tiles. */
	private static final String CHILDREN = "children";

	/** Descriptor key holding a tile's id. */
	private static final String TILE_ID = "id";

	/** Argument of the {@link ReactDashboardControl#REORDER_COMMAND} holding the new order. */
	private static final String ORDER_ARG = "order";

	private ViewChannel _projectInput;

	private ViewContext _context;

	private CommandScope _scope;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_projectInput = new DefaultViewChannel(PROJECT_INPUT);
		_scope = new CommandScope(List.of());
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")))
				.withScope(CommandScope.class, _scope);
		_context.registerChannel(PROJECT_INPUT, _projectInput);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_scope = null;
		_projectInput = null;
		super.tearDown();
	}

	/**
	 * The dashboard applies the order and the hidden tiles the arrangement yields; a stored id
	 * without a tile is skipped. Another displayed object brings its own arrangement.
	 */
	public void testArrangementApplied() throws Exception {
		storeArrangement(_project, LAST + ",gone," + FIRST, Set.of(SECOND));
		storeArrangement(_otherProject, SECOND + "," + LAST, Set.of());
		_projectInput.set(_project);
		// Reads both projects.
		becomeUser(_root);

		ReactDashboardControl dashboard = storedDashboard();
		dashboard.attach();
		assertEquals(List.of(LAST, FIRST), tileIds(dashboard));
		assertFalse(dashboard.isTileVisible(SECOND));

		_projectInput.set(_otherProject);
		assertEquals("Another object is displayed with its own arrangement.", List.of(SECOND, LAST, FIRST),
			tileIds(dashboard));
	}

	/**
	 * Without a stored arrangement, the tiles appear in the order of their declaration.
	 */
	public void testNoStoredArrangement() throws Exception {
		_projectInput.set(_project);
		becomeUser(_responsible);

		ReactDashboardControl dashboard = storedDashboard();
		dashboard.attach();
		assertEquals(List.of(FIRST, SECOND, LAST), tileIds(dashboard));
	}

	/**
	 * Rearranging and hiding a tile runs the store function with the order and the hidden tiles,
	 * and its changes are committed.
	 */
	public void testStoreArrangement() throws Exception {
		_projectInput.set(_project);
		becomeUser(_responsible);

		ReactDashboardControl dashboard = storedDashboard();
		dashboard.attach();
		dashboard.enterEditMode();

		assertNull(CommandFailures.outcome(() -> dashboard.executeClientCommand(
			ReactDashboardControl.REORDER_COMMAND, Map.of(ORDER_ARG, List.of(LAST, FIRST, SECOND)))));
		assertEquals(LAST + "," + FIRST + "," + SECOND, _project.tValueByName(ORDER_ATTRIBUTE));

		assertNull(CommandFailures.outcome(() -> dashboard.executeClientCommand(
			ReactDashboardControl.HIDE_COMMAND, Map.of(HideTileArguments.TILE_ID, FIRST))));
		assertEquals(Set.of(FIRST), Set.copyOf((Collection<?>) _project.tValueByName(HIDDEN_ATTRIBUTE)));

		ReactDashboardControl reopened = storedDashboard();
		assertEquals("The stored arrangement applies when the dashboard is displayed again.",
			List.of(LAST, SECOND), tileIds(reopened));
	}

	/**
	 * A user the arrangement access refuses is offered no editing of the layout; one it allows is.
	 */
	public void testArrangementAccess() throws Exception {
		_projectInput.set(_project);

		becomeUser(_roleless);
		ReactDashboardControl refused = storedDashboard();
		refused.attach();
		assertEquals("No edit command without the right to store the arrangement.", List.of(), layoutCommands());
		refused.detach();

		becomeUser(_responsible);
		ReactDashboardControl allowed = storedDashboard();
		allowed.attach();
		assertTrue(layoutCommands().contains(DashboardCommandModel.EDIT_COMMAND));

		_projectInput.set(_otherProject);
		assertEquals("No edit command for an object the user may not change.", List.of(), layoutCommands());
	}

	/**
	 * A reorder and a hide a user without the right to store the arrangement sends anyway change
	 * nothing: neither the displayed arrangement nor the stored one.
	 */
	public void testRefusedRearrangement() throws Exception {
		_projectInput.set(_project);
		becomeUser(_roleless);

		ReactDashboardControl dashboard = storedDashboard();
		dashboard.attach();
		assertFalse(dashboard.isRearrangeable());

		reorder(dashboard, List.of(LAST, SECOND, FIRST));
		assertNull(CommandFailures.outcome(() -> dashboard.executeClientCommand(
			ReactDashboardControl.HIDE_COMMAND, Map.of(HideTileArguments.TILE_ID, FIRST))));

		assertEquals(List.of(FIRST, SECOND, LAST), tileIds(dashboard));
		assertTrue(dashboard.isTileVisible(FIRST));
		assertNull("Nothing stored.", _project.tValueByName(ORDER_ATTRIBUTE));
		assertEquals("Nothing stored.", Set.of(), Set.copyOf((Collection<?>) _project.tValueByName(HIDDEN_ATTRIBUTE)));
	}

	/**
	 * With an arrangement but no store function, the arrangement is displayed but not edited.
	 */
	public void testArrangementWithoutStore() throws Exception {
		storeArrangement(_project, LAST, Set.of());
		_projectInput.set(_project);
		becomeUser(_responsible);

		ReactDashboardControl dashboard = createDashboard("""
			arrangement='%s'
			""".formatted(arrangementScript()));
		dashboard.attach();
		assertEquals(List.of(LAST, FIRST, SECOND), tileIds(dashboard));
		assertEquals(List.of(), layoutCommands());
	}

	/**
	 * With personalize-by, the personal arrangement is stored per displayed object; without it,
	 * under the key of the dashboard alone.
	 */
	public void testPersonalizeBy() throws Exception {
		// Reads both projects.
		becomeUser(_root);
		_projectInput.set(_project);

		ReactDashboardControl dashboard = createDashboard("personalize-by='$project'");
		dashboard.attach();
		reorder(dashboard, List.of(LAST, FIRST, SECOND));

		_projectInput.set(_otherProject);
		assertEquals("The other object has no arrangement yet.", List.of(FIRST, SECOND, LAST), tileIds(dashboard));
		reorder(dashboard, List.of(SECOND, FIRST, LAST));

		assertEquals(List.of(LAST, FIRST, SECOND), personal(orderKey(_project)));
		assertEquals(List.of(SECOND, FIRST, LAST), personal(orderKey(_otherProject)));
		assertNull("Nothing stored under the key of the dashboard alone.",
			personal("dashboard.tileOrder." + DASHBOARD));

		_projectInput.set(_project);
		assertEquals(List.of(LAST, FIRST, SECOND), tileIds(dashboard));

		ReactDashboardControl plain = createDashboard("");
		plain.attach();
		reorder(plain, List.of(LAST, SECOND, FIRST));
		assertEquals("Without personalize-by, the order is stored under the key of the dashboard.",
			List.of(LAST, SECOND, FIRST), personal("dashboard.tileOrder." + DASHBOARD));
	}

	/**
	 * The value the current user stored under the given key of the personal configuration.
	 */
	private static Object personal(String key) {
		return PersonalConfiguration.getPersonalConfiguration().getJSONValue(key);
	}

	private static String orderKey(TLObject project) {
		return "dashboard.tileOrder." + DASHBOARD + DashboardElement.PC_KEY_ADDITION_SEPARATOR
			+ IdentifierUtil.toExternalForm(project.tIdLocal());
	}

	private static void reorder(ReactDashboardControl dashboard, List<String> order) {
		assertNull(CommandFailures.outcome(() -> dashboard.executeClientCommand(
			ReactDashboardControl.REORDER_COMMAND, Map.of(ORDER_ARG, order))));
	}

	private List<String> layoutCommands() {
		List<String> result = new ArrayList<>();
		for (CommandModel command : _scope.getAllCommands()) {
			if (command instanceof DashboardCommandModel) {
				result.add(command.getName());
			}
		}
		return result;
	}

	private void storeArrangement(TLObject project, String order, Set<String> hidden) {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			project.tUpdateByName(ORDER_ATTRIBUTE, order);
			project.tUpdateByName(HIDDEN_ATTRIBUTE, hidden);
			tx.commit();
		}
	}

	private static String attribute(String name) {
		return "`" + qualified(PROJECT) + "#" + name + "`";
	}

	private static String arrangementScript() {
		return "if($project.get(%1$s) == null, null, {\"order\": $project.get(%1$s).split(\",\"), \"hidden\": $project.get(%2$s)})"
			.formatted(attribute(ORDER_ATTRIBUTE), attribute(HIDDEN_ATTRIBUTE));
	}

	/**
	 * A dashboard storing its arrangement at the displayed project, which only a user who may
	 * write the project may change.
	 */
	private ReactDashboardControl storedDashboard() {
		String store = "{$project.set(%s, $order.join(\",\")); $project.set(%s, $hidden);}"
			.formatted(attribute(ORDER_ATTRIBUTE), attribute(HIDDEN_ATTRIBUTE));
		return createDashboard("""
			arrangement='%s'
			store-arrangement='%s'
			""".formatted(arrangementScript(), store),
			"""
			<arrangement-access>
				<operation name="Write" object="$project" type="%s"/>
			</arrangement-access>
			""".formatted(qualified(PROJECT)));
	}

	private ReactDashboardControl createDashboard(String attributes) {
		return createDashboard(attributes, "");
	}

	private ReactDashboardControl createDashboard(String attributes, String content) {
		String view = """
			<view>
				<dashboard id="%s" inputs="%s" %s>
					%s
					<tile id="%s" width="half"><card/></tile>
					<tile id="%s" width="half"><card/></tile>
					<tile id="%s" width="half"><card/></tile>
				</dashboard>
			</view>
			""".formatted(DASHBOARD, PROJECT_INPUT, attributes, content, FIRST, SECOND, LAST);
		try {
			ViewElement.Config config =
				ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, INLINE_VIEW)));
			DefaultInstantiationContext instantiation =
				new DefaultInstantiationContext(TestDashboardArrangement.class);
			DashboardElement element = (DashboardElement) instantiation.getInstance(config.getContent());
			instantiation.checkErrors();
			return (ReactDashboardControl) element.createControl(_context);
		} catch (ConfigurationException ex) {
			throw new AssertionError("The dashboard view does not parse.", ex);
		}
	}

	private static List<String> tileIds(ReactDashboardControl dashboard) {
		String state = dashboard.stateAsJSON();
		List<String> result = new ArrayList<>();
		try {
			for (Object child : (List<?>) ((Map<?, ?>) JSON.fromString(state)).get(CHILDREN)) {
				result.add((String) ((Map<?, ?>) child).get(TILE_ID));
			}
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a dashboard: " + state, ex);
		}
		return result;
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return suiteWith(TestDashboardArrangement.class, ThemeFactory.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, LabelProviderService.Module.INSTANCE);
	}

}
