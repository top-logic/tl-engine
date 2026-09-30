/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import com.top_logic.basic.config.annotation.Mandatory;
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
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.layout.view.command.WithTransactionAction;
import com.top_logic.layout.view.security.ModelAccessRule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.util.Resources;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests the {@link ModelAccessRule} and the executability rules that actions bring of their own.
 */
public class TestModelAccessRule extends AbstractModelAccessTest {

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
		ViewExecutabilityRule rule = rule("<model-access operation='Delete'/>");

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_task));
	}

	/**
	 * Deleting an object the user has no right on is disabled, naming the operation.
	 */
	public void testDeleteDeniedDisabled() throws Exception {
		ViewExecutabilityRule rule = rule("<model-access operation='Delete'/>");

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_DELETE_DENIED,
			rule.isExecutable(_project));
	}

	/**
	 * Deleting an object of a type granting the deletion to no role at all is hidden.
	 */
	public void testDeleteGrantedToNoRoleHidden() throws Exception {
		ViewExecutabilityRule rule = rule("<model-access operation='Delete'/>");

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_category));
	}

	/**
	 * A restricted user may never delete, so the command is hidden.
	 */
	public void testRestrictedUserHidden() throws Exception {
		ViewExecutabilityRule rule = rule("<model-access operation='Delete'/>");
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_responsible.setRestrictedUser(Boolean.TRUE);
			tx.commit();
		}

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_project));
	}

	/**
	 * An attribute-level check decides by the rights on the attribute.
	 */
	public void testAttributeWrite() throws Exception {
		ViewExecutabilityRule name = rule("<model-access operation='Write' attribute='" + NAME + "'/>");
		ViewExecutabilityRule secret = rule("<model-access operation='Write' attribute='" + SECRET + "'/>");

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, name.isExecutable(_project));
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_ATTRIBUTE_WRITE_DENIED__ATTRIBUTE,
			secret.isExecutable(_project));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, secret.isExecutable(_project));
	}

	/**
	 * A creation without container is checked against the security root; refused, it is hidden.
	 */
	public void testTopLevelCreateDeniedHidden() throws Exception {
		ViewExecutabilityRule rule = rule("<model-access operation='Create' type='" + qualified(PROJECT) + "'/>");

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
			rule("<model-access operation='Create' container='" + CHANNEL + "' reference='" + TASKS + "'/>");
		_channel.set(_project);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE,
			rule.isExecutable(null));
	}

	/**
	 * The programmatic creation check agrees with the configured one.
	 */
	public void testCreationFactory() {
		ViewExecutabilityRule rule = ModelAccessRule.creation(null, new ChannelRef(CHANNEL), TASKS);
		((ContextDependentRule) rule).bind(_context);
		_channel.set(_project);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE,
			rule.isExecutable(null));
	}

	/**
	 * The configured display overrides the derived one.
	 */
	public void testDeniedOverride() throws Exception {
		ViewExecutabilityRule disabledCreate =
			rule("<model-access operation='Create' type='" + qualified(PROJECT) + "' denied='disable'/>");
		ViewExecutabilityRule hiddenDelete = rule("<model-access operation='Delete' denied='hide'/>");

		becomeUser(_roleless);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE,
			disabledCreate.isExecutable(null));
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, hiddenDelete.isExecutable(_project));
	}

	/**
	 * Without an object or container there is nothing to check: the rule leaves that to rules
	 * deciding about a missing input.
	 */
	public void testNothingToCheck() throws Exception {
		ViewExecutabilityRule onInput = rule("<model-access operation='Delete'/>");
		ViewExecutabilityRule onChannel = rule("<model-access operation='Delete' object='" + CHANNEL + "'/>");
		ViewExecutabilityRule inContainer =
			rule("<model-access operation='Create' container='" + CHANNEL + "' reference='" + TASKS + "'/>");

		becomeUser(_roleless);
		assertSame(ExecutableState.EXECUTABLE, onInput.isExecutable(null));
		assertSame(ExecutableState.EXECUTABLE, onChannel.isExecutable(_project));
		assertSame(ExecutableState.EXECUTABLE, inContainer.isExecutable(null));
	}

	/**
	 * The object channel is observed: a new object is a new check.
	 */
	public void testObjectChannel() throws Exception {
		ModelAccessRule rule = (ModelAccessRule) rule("<model-access operation='Delete' object='" + CHANNEL + "'/>");
		int[] revalidations = { 0 };
		Runnable stop = rule.observe(() -> revalidations[0]++);

		becomeUser(_responsible);
		_channel.set(_project);
		assertEquals(1, revalidations[0]);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		_channel.set(_category);
		assertEquals(2, revalidations[0]);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));

		stop.run();
		_channel.set(_project);
		assertEquals(2, revalidations[0]);
	}

	/**
	 * A custom command group is checked like the built-in ones, the reason naming the operation.
	 */
	public void testCustomCommandGroup() throws Exception {
		ViewExecutabilityRule rule = rule("<model-access operation='" + FINISH + "'/>");

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
		assertSame("No role may finish a category.", ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_category));

		becomeUser(_roleless);
		ExecutableState state = rule.isExecutable(_project);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_OPERATION_DENIED__OPERATION, state);
		assertEquals("You are not allowed to perform the operation \"Finish\" on this object.",
			Resources.getInstance(Locale.ENGLISH).getString(state.getI18NReasonKey()));
	}

	/**
	 * A configuration not describing a check is reported.
	 */
	public void testInvalidConfig() throws ConfigurationException {
		assertInvalid("<model-access operation='Create' reference='" + TASKS + "'/>");
		assertInvalid("<model-access operation='Delete' container='" + CHANNEL + "' reference='" + TASKS + "'/>");
		assertInvalid("<model-access operation='Write' type='" + qualified(PROJECT) + "' attribute='" + NAME + "'/>");
		assertInvalid("<model-access operation='Create' attribute='" + NAME + "'/>");
		assertInvalid("<model-access operation='Delete' object='" + CHANNEL + "' type='" + qualified(PROJECT) + "'/>");
	}

	/**
	 * Without a type, a creation creates an object of the type of the (transient) command input.
	 */
	public void testCreateTypeOfInput() throws Exception {
		ViewExecutabilityRule rule = rule("<model-access operation='Create'/>");
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
		ViewExecutabilityRule rule = rule("<model-access operation='Create' container='" + CHANNEL + "'/>");
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
			instantiate(ruleAction("Delete", null)),
			instantiate(ruleAction(FINISH, CHANNEL))));
		ViewExecutabilityRule rule =
			ViewExecutabilityRules.withIntrinsicRule(_context, command, NullInputDisabled.INSTANCE);

		becomeUser(_responsible);
		_channel.set(_project);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
		assertSame("The configured rule takes part.", ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));

		_channel.set(_category);
		assertSame("The rule of the second action reads its bound channel.", ExecutableState.NOT_EXEC_HIDDEN,
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
		config.getActions().add(ruleAction("Delete", null));
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

	private ViewExecutabilityRule rule(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestModelAccessRule.class);
		ViewExecutabilityRule rule = context.getInstance(parse(context, xml));
		context.checkErrors();
		((ContextDependentRule) rule).bind(_context);
		return rule;
	}

	private void assertInvalid(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestModelAccessRule.class);
		context.getInstance(parse(context, xml));
		assertTrue("Expected an error for: " + xml, context.hasErrors());
	}

	private static ModelAccessRule.Config parse(InstantiationContext context, String xml)
			throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			ModelAccessRule.Config.TAG_NAME, TypedConfiguration.getConfigurationDescriptor(ModelAccessRule.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		return (ModelAccessRule.Config) reader.read();
	}

	private static RuleAction.Config ruleAction(String operation, String channel) {
		RuleAction.Config config = TypedConfiguration.newConfigItem(RuleAction.Config.class);
		config.setOperation(new CommandGroupReference(operation));
		config.setObject(channel == null ? null : new ChannelRef(channel));
		return config;
	}

	private static ViewAction instantiate(PolymorphicConfiguration<? extends ViewAction> config) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestModelAccessRule.class);
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

	private static void assertDisabled(String expectedReasonKey, ExecutableState state) {
		assertTrue("Expected a visible command, got: " + state, state.isVisible());
		assertFalse("Expected a disabled command, got: " + state, state.isExecutable());
		assertEquals(expectedReasonKey, state.getI18NReasonKey().getKey());
	}

	/**
	 * {@link ViewAction} passing its input on, bringing a {@link ModelAccessRule} of its own.
	 */
	public static class RuleAction implements ViewAction {

		/**
		 * Configuration of a {@link RuleAction}.
		 */
		public interface Config extends PolymorphicConfiguration<RuleAction> {

			/** Configuration name for {@link #getOperation()}. */
			String OPERATION = "operation";

			/** Configuration name for {@link #getObject()}. */
			String OBJECT = "object";

			@Override
			@ClassDefault(RuleAction.class)
			Class<? extends RuleAction> getImplementationClass();

			/**
			 * The operation the action performs.
			 */
			@Name(OPERATION)
			@Mandatory
			CommandGroupReference getOperation();

			/**
			 * @see #getOperation()
			 */
			void setOperation(CommandGroupReference value);

			/**
			 * Channel holding the object operated on, the command input when unset.
			 */
			@Name(OBJECT)
			@Nullable
			@Format(ChannelRefFormat.class)
			ChannelRef getObject();

			/**
			 * @see #getObject()
			 */
			void setObject(ChannelRef value);
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
			ModelAccessRule.Config config = TypedConfiguration.newConfigItem(ModelAccessRule.Config.class);
			config.setOperation(_config.getOperation());
			config.setObject(_config.getObject());
			return ModelAccessRule.create(config);
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestModelAccessRule.class);
	}
}
