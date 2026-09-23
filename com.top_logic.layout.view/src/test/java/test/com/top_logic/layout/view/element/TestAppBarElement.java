/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.view.command.FakeCommandModelBase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.react.control.layout.ToolbarOverflow;
import com.top_logic.layout.react.control.nav.ReactAppBarControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.element.AppBarElement;

/**
 * Tests the actions of an {@link AppBarElement}: the commands placed in the bar form one toolbar,
 * which collapses towards the bar's trailing end and follows the commands of the surrounding
 * scope.
 *
 * <p>
 * The element is exercised through its public seam - a view read the way the application reads it,
 * a control created for a view context whose scope the test feeds, and the client state that
 * control publishes.
 * </p>
 */
public class TestAppBarElement extends TestCase {

	/** State key naming the React component a control descriptor stands for. */
	private static final String MODULE = "module";

	/** State key holding the client state of a control descriptor. */
	private static final String STATE = "state";

	/** The React component of a toolbar. */
	private static final String TOOLBAR_MODULE = "TLToolbar";

	private static final String VIEW = """
			<view>
				<app-bar/>
			</view>
			""";

	/** The bar's actions are a toolbar, whatever the scope currently holds. */
	public void testActionsAreAToolbar() throws Exception {
		Map<?, ?> actions = actions(createAppBar(new CommandScope(List.of())));

		assertEquals("The bar's actions are one toolbar, not a list of buttons.",
			TOOLBAR_MODULE, actions.get(MODULE));
	}

	/**
	 * A toolbar reads from the left, so the bar folds the commands that do not fit into an
	 * overflow menu at its trailing end.
	 */
	public void testActionsCollapseFromTheTrailingEnd() throws Exception {
		Map<?, ?> actions = actions(createAppBar(new CommandScope(List.of(command(CommandPlacement.TOOLBAR)))));

		assertEquals(ToolbarOverflow.TRAILING.getExternalName(),
			toolbarState(actions).get(ReactToolbarControl.OVERFLOW));
		assertEquals("The command placed in the bar is its one group.", 1, groups(actions).size());
	}

	/** Only the commands placed in the bar become buttons of it. */
	public void testOtherPlacementsStayOutOfTheBar() throws Exception {
		Map<?, ?> actions =
			actions(createAppBar(new CommandScope(List.of(command(CommandPlacement.CONTEXT_MENU)))));

		assertEquals("A command of another placement contributes no group.", 0, groups(actions).size());
	}

	/**
	 * A command contributed to the scope after the bar was built reaches the toolbar on display,
	 * which keeps collapsing the way the bar asks for.
	 */
	public void testCommandAddedLaterReachesTheBar() throws Exception {
		CommandScope scope = new CommandScope(List.of());
		ReactControl appBar = createAppBar(scope);
		assertEquals("The bar starts without commands.", 0, groups(actions(appBar)).size());

		scope.addCommand(command(CommandPlacement.TOOLBAR));

		Map<?, ?> actions = actions(appBar);
		assertEquals("The command added to the scope reaches the bar.", 1, groups(actions).size());
		assertEquals("The rebuilt toolbar keeps collapsing towards the bar's trailing end.",
			ToolbarOverflow.TRAILING.getExternalName(), toolbarState(actions).get(ReactToolbarControl.OVERFLOW));
	}

	/** A command removed from the scope disappears from the bar. */
	public void testCommandRemovedFromTheScopeLeavesTheBar() throws Exception {
		CommandModel command = command(CommandPlacement.TOOLBAR);
		CommandScope scope = new CommandScope(List.of(command));
		ReactControl appBar = createAppBar(scope);

		scope.removeCommand(command);

		assertEquals("The command withdrawn from the scope leaves the bar.",
			0, groups(actions(appBar)).size());
	}

	/** The descriptor of the bar's actions toolbar. */
	private static Map<?, ?> actions(ReactControl appBar) {
		Object actions = state(appBar).get(ReactAppBarControl.ACTIONS);
		assertNotNull("The bar always carries an actions toolbar, so commands added later have a"
			+ " target for the rebuild.", actions);
		return (Map<?, ?>) actions;
	}

	private static Map<?, ?> toolbarState(Map<?, ?> actions) {
		return (Map<?, ?>) actions.get(STATE);
	}

	private static List<?> groups(Map<?, ?> actions) {
		return (List<?>) toolbarState(actions).get(ReactToolbarControl.GROUPS);
	}

	/** A command that does nothing but state where it is placed. */
	private static CommandModel command(CommandPlacement placement) {
		return new FakeCommandModelBase("test") {
			@Override
			public CommandPlacement getPlacement() {
				return placement;
			}
		};
	}

	/** The control of an app bar placed in the given command scope. */
	private static ReactControl createAppBar(CommandScope scope) throws ConfigurationException {
		DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(TestAppBarElement.class);
		UIElement element = instantiationContext.getInstance(appBarConfig());
		instantiationContext.checkErrors();

		ViewContext context = new DefaultViewContext(new DefaultReactContext("", "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test")))
				.withScope(CommandScope.class, scope);

		return (ReactControl) element.createControl(context);
	}

	/** The {@code app-bar} configuration of the test view. */
	private static PolymorphicConfiguration<? extends UIElement> appBarConfig() throws ConfigurationException {
		ViewElement.Config config =
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(VIEW, "test-app-bar.view.xml")));
		PolymorphicConfiguration<? extends UIElement> content = config.getContent();
		assertTrue("The view shows an app bar, not " + content, content instanceof AppBarElement.Config);
		return content;
	}

	private static Map<?, ?> state(ReactControl appBar) {
		String json = appBar.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view.
	 */
	public static Test suite() throws ModuleException {
		return ServiceTestSetup.createSetup(TestAppBarElement.class, TypeIndex.Module.INSTANCE);
	}
}
