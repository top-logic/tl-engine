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

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.DeleteObjectAction;
import com.top_logic.layout.view.command.DeleteVetoDisabled;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.layout.view.command.WithTransactionAction;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.security.IntrinsicAccessRules;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests the {@link IntrinsicAccessRules}, the {@link DeleteVetoDisabled} rule, and the executability
 * rules that actions bring of their own.
 */
public class TestIntrinsicAccessRules extends AbstractModelAccessTest {

	/** Name of the channel holding the object or container in the tests. */
	private static final String CHANNEL = "subject";

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
	 * Deleting the command input is offered to the user holding the right on it.
	 */
	public void testDeleteAllowed() throws Exception {
		ViewExecutabilityRule rule = delete();

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_task));
	}

	/**
	 * Deleting an object the user has no right on is disabled, naming the operation.
	 */
	public void testDeleteDeniedDisabled() throws Exception {
		ViewExecutabilityRule rule = delete();

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_DELETE_DENIED,
			rule.isExecutable(_project));
	}

	/**
	 * Deleting an object of a type granting the deletion to no role at all is hidden.
	 */
	public void testDeleteGrantedToNoRoleHidden() throws Exception {
		ViewExecutabilityRule rule = delete();

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_category));
	}

	/**
	 * A restricted user may never delete, so the command is hidden.
	 */
	public void testRestrictedUserHidden() throws Exception {
		ViewExecutabilityRule rule = delete();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_responsible.setRestrictedUser(Boolean.TRUE);
			tx.commit();
		}

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_project));
	}

	/**
	 * A creation without container is checked against the security root; refused, it is hidden.
	 */
	public void testTopLevelCreateDeniedHidden() throws Exception {
		ViewExecutabilityRule rule = creation(TLModelPartRef.ref(qualified(PROJECT)), null, null);

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));
	}

	/**
	 * A creation in a container is checked in the context of the container; refused, it is
	 * disabled.
	 */
	public void testCreateInContainer() throws Exception {
		ViewExecutabilityRule rule =
			creation(null, CHANNEL, TASKS);
		_channel.set(_project);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE,
			rule.isExecutable(null));
	}

	/**
	 * Without a processed object there is nothing to delete, and with an empty container channel
	 * there is nothing to add a created object to: both disable the command.
	 */
	public void testNothingToCheck() throws Exception {
		ViewExecutabilityRule onInput = delete();
		ViewExecutabilityRule inContainer =
			creation(null, CHANNEL, TASKS);

		becomeUser(_roleless);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, onInput.isExecutable(null));
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, inContainer.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, inContainer.isExecutable(null));
	}

	/**
	 * With an empty container channel and without reference, the object is created without
	 * container: the creation is checked against the security root, refused, it is hidden.
	 */
	public void testCreateInEmptyContainer() throws Exception {
		ViewExecutabilityRule rule = creation(TLModelPartRef.ref(qualified(PROJECT)), CHANNEL, null);

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));

		becomeUser(_roleless);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));
	}

	/**
	 * With an empty container channel and neither type nor reference, the type of the (transient)
	 * command input is checked against the security root.
	 */
	public void testCreateTypeOfInputInEmptyContainer() throws Exception {
		ViewExecutabilityRule rule = creation(null, CHANNEL, null);
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(PROJECT));

		becomeUser(_responsible);
		assertSame("Checked against the security root.", ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(draft));
		assertSame("Nothing to check.", ExecutableState.EXECUTABLE, rule.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(draft));
	}

	/**
	 * Deleting a collection is checked on every element; an empty collection is no object.
	 */
	public void testDeleteCollection() throws Exception {
		TLObject other = newObject(qualified(PROJECT), "other");
		ViewExecutabilityRule rule = delete();

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(List.of(_project, _task)));
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_DELETE_DENIED,
			rule.isExecutable(List.of(_project, other)));
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_DELETE_DENIED,
			rule.isExecutable(List.of(other, _project)));
		assertSame("A type no role may delete hides the command.", ExecutableState.NOT_EXEC_HIDDEN,
			rule.isExecutable(List.of(_project, _category)));
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(List.of()));
	}

	/**
	 * A command deleting a collection is offered only when every element may be deleted and none
	 * vetoes its deletion.
	 */
	public void testDeleteObjectActionCollection() throws Exception {
		ViewAction delete = instantiate(TypedConfiguration.newConfigItem(DeleteObjectAction.Config.class));
		ViewExecutabilityRule rule = ViewExecutabilityRules.withIntrinsicRule(_context,
			new GenericViewCommand(List.of(delete)), ViewExecutabilityRule.ALWAYS_EXECUTABLE);
		Person anonymous = PersonManager.getManager().getAnonymous();

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(List.of(_project, _roleless)));
		// The approval of the deletion refuses the anonymous account before its veto is asked.
		assertDisabled(
			com.top_logic.knowledge.gui.layout.person.I18NConstants.ERROR_NOT_EXECUTABLE_FOR_ANONYMOUS_ACCOUNT,
			rule.isExecutable(List.of(_project, anonymous)));
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(List.of()));

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_DELETE_DENIED,
			rule.isExecutable(List.of(_project)));
	}

	/**
	 * A creation into a reference of an abstract type is allowed where one of its concrete
	 * specializations may be created.
	 */
	public void testCreateAbstractReferenceType() throws Exception {
		ViewExecutabilityRule rule = creation(null, CHANNEL, "deliverables");
		_channel.set(_project);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE,
			rule.isExecutable(null));
	}

	/**
	 * A container that is no single object is refused: the action creates in one container only.
	 */
	public void testCreateInCollectionContainer() throws Exception {
		ViewExecutabilityRule rule = creation(null, CHANNEL, TASKS);
		_channel.set(List.of(_project));

		becomeUser(_responsible);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_CREATE_DENIED, rule.isExecutable(null));
	}

	/**
	 * The container channel is observed: a new container is a new check.
	 */
	public void testContainerChannel() throws Exception {
		ViewExecutabilityRule rule = creation(null, CHANNEL, TASKS);
		int[] revalidations = { 0 };
		Runnable stop = ((ObservableRule) rule).observe(() -> revalidations[0]++);
		TLObject other = newObject(qualified(PROJECT), "other");

		becomeUser(_responsible);
		_channel.set(_project);
		assertEquals(1, revalidations[0]);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		_channel.set(other);
		assertEquals(2, revalidations[0]);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE,
			rule.isExecutable(null));

		_channel.set(null);
		assertEquals(3, revalidations[0]);
		assertSame("Nothing to add the created object to.", ExecutableState.NO_EXEC_NO_MODEL,
			rule.isExecutable(null));

		_channel.set(_project);
		assertEquals(4, revalidations[0]);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		stop.run();
		_channel.set(null);
		assertEquals(4, revalidations[0]);
	}

	/**
	 * Switching the container channel of a creation without reference between a container and no
	 * container switches between the check in the container and the check against the security
	 * root.
	 */
	public void testEmptyContainerChannelRevalidates() throws Exception {
		ViewExecutabilityRule rule = creation(TLModelPartRef.ref(qualified(TASK)), CHANNEL, null);
		int[] revalidations = { 0 };
		Runnable stop = ((ObservableRule) rule).observe(() -> revalidations[0]++);

		becomeUser(_responsible);
		_channel.set(_project);
		assertEquals(1, revalidations[0]);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		_channel.set(null);
		assertEquals(2, revalidations[0]);
		assertSame("Checked against the security root.", ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));

		stop.run();
	}

	/**
	 * Without a type, a creation creates an object of the type of the (transient) command input.
	 */
	public void testCreateTypeOfInput() throws Exception {
		ViewExecutabilityRule rule = creation(null, null, null);
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(PROJECT));

		becomeUser(_responsible);
		assertSame("Checked against the security root.", ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(draft));
		assertSame("Nothing to check.", ExecutableState.EXECUTABLE, rule.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(draft));
	}

	/**
	 * Without a type and a reference, a creation in a container creates an object of the type of the
	 * command input.
	 */
	public void testCreateTypeOfInputInContainer() throws Exception {
		ViewExecutabilityRule rule = creation(null, CHANNEL, null);
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(TASK));
		_channel.set(_project);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(draft));

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE,
			rule.isExecutable(draft));
		assertSame("Nothing to check.", ExecutableState.EXECUTABLE, rule.isExecutable(null));
	}

	/**
	 * A command running actions that bring rules of their own decides by all of them, bound to its
	 * context, together with its configured rule.
	 */
	public void testGenericCommandCombinesActionRules() {
		GenericViewCommand command = new GenericViewCommand(List.of(
			instantiate(ruleAction(null)),
			instantiate(ruleAction(CHANNEL))));
		ViewExecutabilityRule rule =
			ViewExecutabilityRules.withIntrinsicRule(_context, command, NullInputDisabled.INSTANCE);

		becomeUser(_responsible);
		_channel.set(_project);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
		assertSame("The configured rule takes part.", ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));

		_channel.set(newObject(qualified(PROJECT), "other"));
		// The rule of the second action reads its bound channel.
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE,
			rule.isExecutable(_project));

		becomeUser(_roleless);
		_channel.set(null);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_DELETE_DENIED,
			rule.isExecutable(_project));
	}

	/**
	 * A transaction forwards the rules of the actions it runs.
	 */
	public void testWithTransactionForwardsRules() {
		WithTransactionAction.Config config = TypedConfiguration.newConfigItem(WithTransactionAction.Config.class);
		config.getActions().add(ruleAction(null));
		ViewAction transaction = instantiate(config);
		GenericViewCommand command = new GenericViewCommand(List.of(transaction));

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, transaction.getIntrinsicRule().isExecutable(_project));
		assertSame(ExecutableState.EXECUTABLE, command.getIntrinsicRule().isExecutable(_project));

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_DELETE_DENIED,
			transaction.getIntrinsicRule().isExecutable(_project));
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_DELETE_DENIED,
			command.getIntrinsicRule().isExecutable(_project));
	}

	/**
	 * Actions without a rule of their own leave the decision to the command.
	 */
	public void testActionsWithoutRule() {
		ViewAction plain = (context, input) -> input;
		GenericViewCommand command = new GenericViewCommand(List.of(plain));

		assertSame(ViewExecutabilityRule.ALWAYS_EXECUTABLE, command.getIntrinsicRule());
	}

	/**
	 * An object vetoing its deletion disables a command deleting it, giving the veto as reason.
	 */
	public void testDeleteVeto() throws Exception {
		ViewExecutabilityRule rule = rule("<delete-veto-disabled/>");
		Person anonymous = PersonManager.getManager().getAnonymous();

		becomeUser(_root);
		assertDisabled(com.top_logic.knowledge.wrap.person.I18NConstants.ERROR_ANONYMOUS_ACCOUNT_CANNOT_BE_DELETED,
			rule.isExecutable(anonymous));
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_roleless));
		assertSame("Nothing to check.", ExecutableState.EXECUTABLE, rule.isExecutable(null));

		assertDisabled("Each object of a collection is checked.",
			com.top_logic.knowledge.wrap.person.I18NConstants.ERROR_ANONYMOUS_ACCOUNT_CANNOT_BE_DELETED,
			rule.isExecutable(List.of(_project, anonymous, _roleless)));
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(List.of(_project, _roleless)));
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(List.of()));
	}

	/**
	 * Deleting an object brings both the deletion right and the delete veto as its rule.
	 */
	public void testDeleteObjectActionRule() {
		ViewAction delete = instantiate(TypedConfiguration.newConfigItem(DeleteObjectAction.Config.class));
		ViewExecutabilityRule rule =
			ViewExecutabilityRules.withIntrinsicRule(_context, new GenericViewCommand(List.of(delete)),
				NullInputDisabled.INSTANCE);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_DELETE_DENIED,
			rule.isExecutable(_project));

		becomeUser(_root);
		ExecutableState anonymous = rule.isExecutable(PersonManager.getManager().getAnonymous());
		assertTrue("Deleting the anonymous account is disabled, got: " + anonymous, anonymous.isDisabled());
	}

	private static ViewExecutabilityRule delete() {
		return IntrinsicAccessRules.onInput(SimpleBoundCommandGroup.DELETE);
	}

	private ViewExecutabilityRule creation(TLModelPartRef type, String container, String reference) {
		ViewExecutabilityRule rule =
			IntrinsicAccessRules.creation(type, container == null ? null : new ChannelRef(container), reference);
		if (rule instanceof ContextDependentRule contextDependent) {
			contextDependent.bind(_context);
		}
		return rule;
	}

	private ViewExecutabilityRule rule(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestIntrinsicAccessRules.class);
		ViewExecutabilityRule rule = context.getInstance(parse(context, xml));
		context.checkErrors();
		return rule;
	}

	@SuppressWarnings("unchecked")
	private static PolymorphicConfiguration<? extends ViewExecutabilityRule> parse(InstantiationContext context,
			String xml) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(DeleteVetoDisabled.Config.TAG_NAME,
			TypedConfiguration.getConfigurationDescriptor(DeleteVetoDisabled.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		return (PolymorphicConfiguration<? extends ViewExecutabilityRule>) reader.read();
	}

	private static RuleAction.Config ruleAction(String container) {
		RuleAction.Config config = TypedConfiguration.newConfigItem(RuleAction.Config.class);
		config.setContainer(container == null ? null : new ChannelRef(container));
		return config;
	}

	private static ViewAction instantiate(PolymorphicConfiguration<? extends ViewAction> config) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestIntrinsicAccessRules.class);
		ViewAction result = context.getInstance(config);
		try {
			context.checkErrors();
		} catch (ConfigurationException ex) {
			throw new AssertionError(ex);
		}
		return result;
	}

	private static void assertDisabled(ResKey expectedReason, ExecutableState state) {
		assertDisabled(expectedReason.getKey(), state);
	}

	private static void assertDisabled(ResKeyTemplate expectedReason, ExecutableState state) {
		assertDisabled(expectedReason.getKey(), state);
	}

	private static void assertDisabled(String message, ResKey expectedReason, ExecutableState state) {
		assertTrue(message + " Expected a disabled command, got: " + state, state.isDisabled());
		assertEquals(message, expectedReason.getKey(), state.getI18NReasonKey().getKey());
	}

	private static void assertDisabled(String expectedReasonKey, ExecutableState state) {
		assertTrue("Expected a visible command, got: " + state, state.isVisible());
		assertFalse("Expected a disabled command, got: " + state, state.isExecutable());
		assertEquals(expectedReasonKey, state.getI18NReasonKey().getKey());
	}

	/**
	 * {@link ViewAction} passing its input on, bringing a rule of its own: the deletion of the
	 * command input, or, with a container, the creation of a task in the container.
	 */
	public static class RuleAction implements ViewAction {

		/**
		 * Configuration of a {@link RuleAction}.
		 */
		public interface Config extends PolymorphicConfiguration<RuleAction> {

			/** Configuration name for {@link #getContainer()}. */
			String CONTAINER = "container";

			@Override
			@ClassDefault(RuleAction.class)
			Class<? extends RuleAction> getImplementationClass();

			/**
			 * Channel holding the project a task is created in, the deletion of the command input
			 * is checked when unset.
			 */
			@Name(CONTAINER)
			@Nullable
			@Format(ChannelRefFormat.class)
			ChannelRef getContainer();

			/**
			 * @see #getContainer()
			 */
			void setContainer(ChannelRef value);
		}

		private final Config _config;

		/**
		 * Creates a {@link RuleAction} from configuration.
		 */
		@CalledByReflection
		public RuleAction(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public Object execute(ReactContext context, Object input) {
			return input;
		}

		@Override
		public ViewExecutabilityRule getIntrinsicRule() {
			if (_config.getContainer() == null) {
				return IntrinsicAccessRules.onInput(SimpleBoundCommandGroup.DELETE);
			}
			return IntrinsicAccessRules.creation(null, _config.getContainer(), TASKS);
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestIntrinsicAccessRules.class);
	}
}
