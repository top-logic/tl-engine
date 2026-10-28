/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.layout.view.element.GridOptions;
import com.top_logic.layout.view.list.LinkElementAction;
import com.top_logic.layout.view.list.ObjectListElement;
import com.top_logic.layout.view.list.ObjectListElement.Layout;
import com.top_logic.layout.view.list.ObjectListItems;
import com.top_logic.layout.view.list.ObjectListScope;
import com.top_logic.layout.view.list.RemoveElementAction;
import com.top_logic.layout.view.security.AccessCheck;
import com.top_logic.layout.view.security.CommandAccess;
import com.top_logic.layout.view.security.CreateAccess;
import com.top_logic.layout.view.security.I18NConstants;
import com.top_logic.layout.view.security.OperationAccess;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests the {@link ObjectListElement.Config#getLinkAccess() link access} and the
 * {@link ObjectListElement.Config#getRemoveAccess() remove access} of an object list, which
 * {@link LinkElementAction &lt;link-element&gt;} and {@link RemoveElementAction &lt;remove-element&gt;}
 * bring as their own rule.
 */
public class TestObjectListAccess extends AbstractModelAccessTest {

	/** The input of the list, holding the project an element is linked to. */
	private static final String PROJECT_INPUT = "project";

	/** The name of the element in the scripts of the list. */
	private static final String ELEMENT = "element";

	/** The channel of the element composed in the new-element content. */
	private static final String NEW_ELEMENT = "newElement";

	private ViewChannel _projectInput;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_projectInput = new DefaultViewChannel(PROJECT_INPUT);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(PROJECT_INPUT, _projectInput);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_projectInput = null;
		super.tearDown();
	}

	/**
	 * Removing an element is offered where the remove access allows it.
	 */
	public void testRemoveAccess() throws ConfigurationException {
		ViewContext list = list(List.of(),
			List.of("<operation name='Delete' object='$element' type='" + qualified(PROJECT) + "'/>"));
		ViewExecutabilityRule remove = rule(new RemoveElementAction(null, null), list);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, remove.isExecutable(_project));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_DELETE_DENIED, remove.isExecutable(_project));
	}

	/**
	 * A remove access no role can ever pass hides the command.
	 */
	public void testRemoveAccessHidden() throws ConfigurationException {
		ViewContext list = list(List.of(),
			List.of("<operation name='Delete' object='$element' type='" + qualified(CATEGORY) + "'/>"));
		ViewExecutabilityRule remove = rule(new RemoveElementAction(null, null), list);

		becomeUser(_responsible);
		assertSame("No role may delete a category.", ExecutableState.NOT_EXEC_HIDDEN, remove.isExecutable(_category));
	}

	/**
	 * Linking an element is offered where the link access allows it; the checks see the inputs of
	 * the list and follow them.
	 */
	public void testLinkAccess() throws ConfigurationException {
		ViewContext list = list(List.of("<create in='$project' reference='" + qualified(PROJECT) + "#" + TASKS + "'/>"),
			List.of());
		ViewExecutabilityRule link = rule(new LinkElementAction(null, null), list);
		int[] revalidations = { 0 };
		Runnable stop = ((ObservableRule) link).observe(() -> revalidations[0]++);

		becomeUser(_responsible);
		assertSame("No project to link to.", ExecutableState.NO_EXEC_NO_MODEL, link.isExecutable(null));
		_projectInput.set(_project);
		assertEquals("A new input value is a new check.", 1, revalidations[0]);
		assertSame(ExecutableState.EXECUTABLE, link.isExecutable(null));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, link.isExecutable(null));
		stop.run();
	}

	/**
	 * The new-element content is displayed only where the link access allows linking in the
	 * current context.
	 */
	public void testNewElementFollowsLinkAccess() throws ConfigurationException {
		ViewContext context = list(List.of("<create in='$project' reference='" + qualified(PROJECT) + "#" + TASKS + "'/>"),
			List.of());
		ObjectListScope scope = context.getScope(ObjectListScope.class);
		_projectInput.set(_project);

		becomeUser(_responsible);
		ObjectListItems items = items(context, scope);
		assertEquals("The responsible composes a task.", 2, items.display().displayedChildren().size());

		_projectInput.set(newObject(qualified(PROJECT), "other"));
		items.showElements(List.of());
		assertEquals("Not in a project of someone else.", 1, items.display().displayedChildren().size());

		becomeUser(_roleless);
		_projectInput.set(_project);
		ObjectListItems refused = items(context, scope);
		assertEquals(1, refused.display().displayedChildren().size());
	}

	/**
	 * Without a link access, the new-element content is displayed to everybody.
	 */
	public void testNewElementWithoutLinkAccess() throws ConfigurationException {
		ViewContext context = list(List.of(), List.of());
		_projectInput.set(_project);

		becomeUser(_roleless);
		ObjectListItems items = items(context, context.getScope(ObjectListScope.class));
		assertEquals(2, items.display().displayedChildren().size());
	}

	private ObjectListItems items(ViewContext context, ObjectListScope scope) {
		ObjectListItems result = new ObjectListItems(context, scope, Layout.LIST,
			TypedConfiguration.newConfigItem(GridOptions.class), List.of(_projectInput), List.of(),
			List.of(new NewElementTemplate()), ELEMENT, NEW_ELEMENT, type(TASK), null);
		result.showElements(List.of());
		return result;
	}

	/**
	 * Content of the new-element slot: a bare control.
	 */
	private static class NewElementTemplate implements UIElement {
		@Override
		public IReactControl createControl(ViewContext context) {
			return new ReactControl(context, null, "TLPanel");
		}
	}

	/**
	 * Without a declaration, nothing is checked.
	 */
	public void testNoDeclaration() throws ConfigurationException {
		ViewContext list = list(List.of(), List.of());

		becomeUser(_roleless);
		assertSame(ExecutableState.EXECUTABLE, rule(new RemoveElementAction(null, null), list).isExecutable(_project));
		assertSame(ExecutableState.EXECUTABLE, rule(new LinkElementAction(null, null), list).isExecutable(null));
		assertSame("Outside a list, nothing is checked.", ExecutableState.EXECUTABLE,
			rule(new RemoveElementAction(null, null), _context).isExecutable(_project));
	}

	/**
	 * The context of the templates of a list with the given input and access checks.
	 */
	private ViewContext list(List<String> linkAccess, List<String> removeAccess) throws ConfigurationException {
		ActionScope scope = ActionScope.of(List.of(PROJECT_INPUT, ELEMENT), ELEMENT);
		ObjectListScope list = new ObjectListScope(List.of(_projectInput), null, null);
		list.initAccess(scope,
			CommandAccess.create(checks(linkAccess), scope, _context, "the link access"),
			CommandAccess.create(checks(removeAccess), scope, _context, "the remove access"));
		return _context.withScope(ObjectListScope.class, list);
	}

	private static ViewExecutabilityRule rule(ViewAction action, ViewContext context) {
		return ViewExecutabilityRules.withIntrinsicRule(context, new GenericViewCommand(List.of(action)),
			ViewExecutabilityRule.ALWAYS_EXECUTABLE);
	}

	private static List<PolymorphicConfiguration<? extends AccessCheck>> checks(List<String> xml)
			throws ConfigurationException {
		List<PolymorphicConfiguration<? extends AccessCheck>> result = new ArrayList<>();
		for (String check : xml) {
			result.add(check(check));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static PolymorphicConfiguration<? extends AccessCheck> check(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestObjectListAccess.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(OperationAccess.Config.TAG_NAME,
			TypedConfiguration.getConfigurationDescriptor(OperationAccess.Config.class));
		descriptors.put(CreateAccess.Config.TAG_NAME,
			TypedConfiguration.getConfigurationDescriptor(CreateAccess.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		PolymorphicConfiguration<? extends AccessCheck> result =
			(PolymorphicConfiguration<? extends AccessCheck>) reader.read();
		context.checkErrors();
		return result;
	}

	private static void assertDisabled(ResKey expectedReason, ExecutableState state) {
		assertTrue("Expected a disabled command, got: " + state, state.isDisabled());
		assertEquals(expectedReason.getKey(), state.getI18NReasonKey().getKey());
	}

	private static void assertDisabled(ResKeyTemplate expectedReason, ExecutableState state) {
		assertTrue("Expected a disabled command, got: " + state, state.isDisabled());
		assertEquals(expectedReason.getKey(), state.getI18NReasonKey().getKey());
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suiteWith(TestObjectListAccess.class, TypeIndex.Module.INSTANCE, CommandCliqueService.Module.INSTANCE);
	}

}
