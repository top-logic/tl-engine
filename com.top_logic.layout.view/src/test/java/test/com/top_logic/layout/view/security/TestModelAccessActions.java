/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

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
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CreateTransientAction;
import com.top_logic.layout.view.command.DeleteObjectAction;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.PersistTransientAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.layout.view.command.WithTransactionAction;
import com.top_logic.layout.view.security.I18NConstants;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests the actions performing a model operation: {@link DeleteObjectAction},
 * {@link CreateTransientAction} and {@link PersistTransientAction}, their enforcement of the model
 * access rights and the executability they bring.
 */
public class TestModelAccessActions extends AbstractModelAccessTest {

	/** Name of the channel holding the container in the tests. */
	private static final String CHANNEL = "container";

	private ViewContext _context;

	private ViewChannel _channel;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(null);
		_channel = new DefaultViewChannel(CHANNEL);
		_context.registerChannel(CHANNEL, _channel);
	}

	@Override
	protected void tearDown() throws Exception {
		_channel = null;
		_context = null;
		super.tearDown();
	}

	/**
	 * The user holding the right deletes the object, and with it the parts of its compositions.
	 */
	public void testDeleteAllowed() throws Exception {
		ViewAction delete = action("<delete-object/>");

		becomeUser(_responsible);
		assertNull(delete.execute(_context, _project));
		assertFalse(_project.tValid());
		assertFalse("The task is part of the project.", _task.tValid());
	}

	/**
	 * Deleting an object without the right is refused with the message of the TL-Script
	 * {@code delete()}, and nothing is deleted.
	 */
	public void testDeleteRefused() throws Exception {
		ViewAction delete = action("<delete-object/>");

		becomeUser(_roleless);
		assertRefused(com.top_logic.model.search.expr.I18NConstants.DELETE_PERMISSION_DENIED__OBJECT,
			() -> delete.execute(_context, _project));
		assertTrue(_project.tValid());
	}

	/**
	 * A deletion nested in an enclosing transaction commits with it; refused, it rolls the enclosing
	 * transaction back.
	 */
	public void testDeleteInTransaction() throws Exception {
		ViewAction transaction = action("<with-transaction><delete-object/></with-transaction>");

		becomeUser(_roleless);
		assertRefused(com.top_logic.model.search.expr.I18NConstants.DELETE_PERMISSION_DENIED__OBJECT,
			() -> transaction.execute(_context, _task));
		assertTrue(_task.tValid());

		becomeUser(_responsible);
		assertNull(transaction.execute(_context, _task));
		assertFalse(_task.tValid());
		assertEquals(List.of(), _project.tValueByName(TASKS));
	}

	/**
	 * A command deleting its input is offered where the user may delete the input.
	 */
	public void testDeleteRule() throws Exception {
		ViewExecutabilityRule rule = commandRule(action("<delete-object/>"));

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
		assertSame("No role may delete a category.", ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_category));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_DELETE_DENIED, rule.isExecutable(_project));
	}

	/**
	 * The draft is a transient object of the type.
	 */
	public void testCreateTransient() throws Exception {
		ViewAction create = action("<create-transient type='" + qualified(PROJECT) + "'/>");

		becomeUser(_roleless);
		TLObject draft = (TLObject) create.execute(_context, _category);
		assertTrue(draft.tTransient());
		assertSame(type(PROJECT), draft.tType());
	}

	/**
	 * Opening the dialog for a top-level creation is hidden where the user may not create the type
	 * at top level.
	 */
	public void testCreateTransientTopLevelRule() throws Exception {
		ViewExecutabilityRule rule = commandRule(action("<create-transient type='" + qualified(PROJECT) + "'/>"));

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));
	}

	/**
	 * Opening the dialog for a creation in a container is disabled where the user may not create in
	 * the container.
	 */
	public void testCreateTransientInContainerRule() throws Exception {
		ViewExecutabilityRule rule = commandRule(action(
			"<create-transient type='" + qualified(TASK) + "' container='" + CHANNEL + "' reference='" + TASKS + "'/>"));
		_channel.set(_project);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, rule.isExecutable(null));
	}

	/**
	 * A reference without a container is reported.
	 */
	public void testReferenceRequiresContainer() {
		assertInvalid("<create-transient type='" + qualified(TASK) + "' reference='" + TASKS + "'/>");
		assertInvalid("<persist-transient reference='" + TASKS + "'/>");
	}

	/**
	 * Persisting a draft at top level requires the right to create the type at top level; refused,
	 * it reports a refused creation.
	 */
	public void testPersistTopLevel() throws Exception {
		ViewAction persist = action("<persist-transient/>");
		TLObject draft = draft(PROJECT, "created");

		becomeUser(_responsible);
		assertRefused(com.top_logic.element.model.copy.I18NConstants.ERROR_PERSIST_PERMISSION_DENIED__TYPE,
			() -> persist.execute(_context, draft));

		becomeUser(_root);
		TLObject created = (TLObject) persist.execute(_context, draft);
		try {
			assertFalse(created.tTransient());
			assertTrue(created.tValid());
			assertSame(type(PROJECT), created.tType());
			assertEquals("created", created.tValueByName(NAME));
		} finally {
			delete(created);
		}
	}

	/**
	 * The Create button of a top-level creation dialog takes the created type from its input, the
	 * draft, and is hidden where the user may not create it.
	 */
	public void testPersistTopLevelRule() throws Exception {
		ViewExecutabilityRule rule = commandRule(action("<persist-transient/>"));
		TLObject draft = draft(PROJECT, "created");

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(draft));
		assertSame("Without a draft, there is nothing to check.", ExecutableState.EXECUTABLE,
			rule.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(draft));
	}

	/**
	 * A configured type decides the rule, independent of the command input.
	 */
	public void testPersistConfiguredTypeRule() throws Exception {
		ViewExecutabilityRule rule = commandRule(action("<persist-transient type='" + qualified(PROJECT) + "'/>"));

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));
	}

	/**
	 * Persisting a draft into a container adds it to the reference of the container.
	 */
	public void testPersistInContainer() throws Exception {
		ViewAction persist =
			action("<persist-transient container='" + CHANNEL + "' reference='" + TASKS + "'/>");
		_channel.set(_project);

		becomeUser(_roleless);
		assertRefused(com.top_logic.element.model.copy.I18NConstants.ERROR_PERSIST_PERMISSION_DENIED__TYPE,
			() -> persist.execute(_context, draft(TASK, "refused")));
		assertEquals(List.of(_task), _project.tValueByName(TASKS));

		becomeUser(_responsible);
		TLObject created = (TLObject) persist.execute(_context, draft(TASK, "created"));
		assertFalse(created.tTransient());
		assertEquals("created", created.tValueByName(NAME));
		assertEquals(List.of(_task, created), _project.tValueByName(TASKS));
	}

	/**
	 * The Create button of a dialog creating in a container is disabled where the user may not
	 * create in the container.
	 */
	public void testPersistInContainerRule() throws Exception {
		ViewExecutabilityRule byReference = commandRule(
			action("<persist-transient container='" + CHANNEL + "' reference='" + TASKS + "'/>"));
		ViewExecutabilityRule byDraft = commandRule(action("<persist-transient container='" + CHANNEL + "'/>"));
		TLObject draft = draft(TASK, "created");
		_channel.set(_project);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, byReference.isExecutable(draft));
		assertSame(ExecutableState.EXECUTABLE, byDraft.isExecutable(draft));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, byReference.isExecutable(draft));
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, byDraft.isExecutable(draft));
	}

	private TLObject draft(String typeName, String name) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(type(typeName));
		result.tUpdateByName(NAME, name);
		return result;
	}

	private ViewExecutabilityRule commandRule(ViewAction action) {
		GenericViewCommand command = new GenericViewCommand(List.of(action));
		return ViewExecutabilityRules.withIntrinsicRule(_context, command, ViewExecutabilityRule.ALWAYS_EXECUTABLE);
	}

	private static ViewAction action(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestModelAccessActions.class);
		ViewAction result = context.getInstance(parse(context, xml));
		context.checkErrors();
		return result;
	}

	private static void assertInvalid(String xml) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestModelAccessActions.class);
		try {
			context.getInstance(parse(context, xml));
		} catch (ConfigurationException ex) {
			return;
		}
		assertTrue("Expected an error for: " + xml, context.hasErrors());
	}

	private static PolymorphicConfiguration<? extends ViewAction> parse(DefaultInstantiationContext context,
			String xml) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(DeleteObjectAction.Config.TAG_NAME, descriptor(DeleteObjectAction.Config.class));
		descriptors.put(CreateTransientAction.Config.TAG_NAME, descriptor(CreateTransientAction.Config.class));
		descriptors.put(PersistTransientAction.Config.TAG_NAME, descriptor(PersistTransientAction.Config.class));
		descriptors.put("with-transaction", descriptor(WithTransactionAction.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<? extends ViewAction> result =
			(PolymorphicConfiguration<? extends ViewAction>) reader.read();
		return result;
	}

	private static ConfigurationDescriptor descriptor(Class<?> configType) {
		return TypedConfiguration.getConfigurationDescriptor(configType);
	}

	private static void assertDisabled(ResKey expectedReason, ExecutableState state) {
		assertDisabled(expectedReason.getKey(), state);
	}

	private static void assertDisabled(ResKeyTemplate expectedReason, ExecutableState state) {
		assertDisabled(expectedReason.getKey(), state);
	}

	private static void assertDisabled(String expectedReasonKey, ExecutableState state) {
		assertTrue("Expected a visible command, got: " + state, state.isVisible());
		assertFalse("Expected a disabled command, got: " + state, state.isExecutable());
		assertEquals(expectedReasonKey, state.getI18NReasonKey().getKey());
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestModelAccessActions.class);
	}
}
