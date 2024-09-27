/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.NoCheckScopeProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * Test case for {@link AbstractCommandHandler}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAbstractCommandHandler extends AbstractLayoutTest {

	public static final String CHECK_CONFIGURED_VALUE = "checkConfiguredValue";

	public static final String CHECK_UNCONFIGURED_VALUE = "checkUnconfiguredValue";

	public static final String GLOBAL_VALUE_UNCONFIGURED = "globalValueUnconfigured";

	public static class TestingCommand extends AbstractCommandHandler {

		public TestingCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	public static class TestingCommandChangeModel extends AbstractCommandHandler {

		static ResKey CHANGED_LABEL = ResKey.text("changedLabel");

		public TestingCommandChangeModel(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {
			getCommandModel(someArguments).setLabel(CHANGED_LABEL);
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	public static class TestingCommandLegacy extends AbstractCommandHandler {

		/** Config interface for {@link TestingCommandLegacy}. */
		public interface Config extends AbstractCommandHandler.Config {

			@Override
			@BooleanDefault(true)
			boolean getConfirm();

			@Override
			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			CommandGroupReference getGroup();

		}

		public TestingCommandLegacy(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		@Deprecated
		protected ResKey getDefaultI18NKey() {
			return I18NConstants.LEGACY_DEFAULT_KEY;
		}

		@Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return TestingRuleNotConfigured.INSTANCE;
		}

		@Override
		@Deprecated
		protected CheckScopeProvider getCheckScopeProvider() {
			return TestingProviderNotConfigured.INSTANCE;
		}

	}

	public static final class TestingRuleConfigured implements ExecutabilityRule {

		public interface Config<TestingRuleConfigured> extends PolymorphicConfiguration<TestingRuleConfigured> {
			String getMyProperty();
		}

		private Config _config;

		/**
		 * Creates a {@link TestAbstractCommandHandler.TestingRuleConfigured} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public TestingRuleConfigured(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			Object checkValue = someValues.get(CHECK_CONFIGURED_VALUE);
			return Utils.equals(checkValue, _config.getMyProperty()) ? ExecutableState.EXECUTABLE
				: ExecutableState.NOT_EXEC_HIDDEN;
		}
	}

	public static final class TestingRuleNotConfigured implements ExecutabilityRule {

		/**
		 * Singleton {@link TestAbstractCommandHandler.TestingRuleNotConfigured} instance.
		 */
		public static final TestingRuleNotConfigured INSTANCE = new TestingRuleNotConfigured();

		private TestingRuleNotConfigured() {
			// Singleton constructor.
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			Object checkValue = someValues.get(CHECK_UNCONFIGURED_VALUE);
			return Utils.equals(checkValue, GLOBAL_VALUE_UNCONFIGURED) ? ExecutableState.EXECUTABLE
				: ExecutableState.NOT_EXEC_HIDDEN;
		}
	}

	public static final class TestingProviderConfigured implements CheckScopeProvider {

		public interface Config extends PolymorphicConfiguration<TestingProviderConfigured> {
			String getProviderProperty();
		}

		private Config _config;

		/**
		 * Creates a {@link TestAbstractCommandHandler.TestingProviderConfigured} from
		 * configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public TestingProviderConfigured(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public CheckScope getCheckScope(LayoutComponent component) {
			return null;
		}

		public String getProviderValue() {
			return _config.getProviderProperty();
		}

	}

	public static final class TestingProviderNotConfigured implements CheckScopeProvider {

		/**
		 * Singleton {@link TestAbstractCommandHandler.TestingProviderNotConfigured} instance.
		 */
		public static final TestingProviderNotConfigured INSTANCE = new TestingProviderNotConfigured();

		private TestingProviderNotConfigured() {
			// Singleton constructor.
		}

		@Override
		public CheckScope getCheckScope(LayoutComponent component) {
			return null;
		}

	}

	public void testDefaults() {
		CommandHandler handler = CommandHandlerFactory.getInstance().getHandler("TestAbstractCommandHandlerDefaults");
		BasicTestCase.assertInstanceof(handler, TestingCommand.class);

		assertEquals("TestAbstractCommandHandlerDefaults", handler.getID());
		assertEquals(SimpleBoundCommandGroup.READ, handler.getCommandGroup());
		assertEquals("default key", Resources.getInstance().getString(handler.getResourceKey(null)));
		assertNull(handler.getConfirmKey(null, Collections.emptyMap()));
		assertNull(handler.getImage(null));
		assertNull(handler.getNotExecutableImage(null));
		assertEquals(ExecutableState.EXECUTABLE,
			handler.isExecutable(null, null, Collections.<String, Object> emptyMap()));
		assertEquals(NoCheckScopeProvider.INSTANCE, handler.checkScopeProvider());
	}

	public void testPropertyConfiguration() {
		CommandHandler handler = CommandHandlerFactory.getInstance().getHandler("TestAbstractCommandHandlerConfigured");
		BasicTestCase.assertInstanceof(handler, TestingCommand.class);

		assertEquals("TestAbstractCommandHandlerConfigured", handler.getID());
		assertEquals(SimpleBoundCommandGroup.SYSTEM, handler.getCommandGroup());
		assertEquals("my.resource.key", handler.getResourceKey(null).getKey());
		assertNotNull(handler.getConfirmKey(null, Collections.emptyMap()));
		assertEquals("/my-image.png", handler.getImage(null).toEncodedForm());
		assertEquals("/my-disabled-image.png", handler.getNotExecutableImage(null).toEncodedForm());
		assertEquals(ExecutableState.EXECUTABLE,
			handler.isExecutable(null, null, Collections.<String, Object> emptyMap()));
		assertEquals(ExecutableState.EXECUTABLE,
			handler.isExecutable(null, null,
				Collections.<String, Object> singletonMap(CHECK_CONFIGURED_VALUE, "anyValue")));
	}

	public void testDefaultsLegacy() {
		CommandHandler handler =
			CommandHandlerFactory.getInstance().getHandler("TestAbstractCommandHandlerLegacyDefaults");

		assertEquals(SimpleBoundCommandGroup.WRITE, handler.getCommandGroup());
		assertEquals("legacy default key", Resources.getInstance().getString(handler.getResourceKey(null)));
		assertNotNull(handler.getConfirmKey(null, Collections.emptyMap()));
		assertEquals(ExecutableState.NOT_EXEC_HIDDEN,
			handler.isExecutable(null, null, Collections.<String, Object> emptyMap()));
		assertEquals(
			ExecutableState.EXECUTABLE,
			handler.isExecutable(null,
				null, Collections.<String, Object> singletonMap(CHECK_UNCONFIGURED_VALUE, GLOBAL_VALUE_UNCONFIGURED)));
		assertEquals(TestingProviderNotConfigured.INSTANCE, handler.checkScopeProvider());
	}

	public void testPropertyLegacyConfiguration() {
		CommandHandler handler =
			CommandHandlerFactory.getInstance().getHandler("TestAbstractCommandHandlerLegacyConfigured");

		assertEquals(SimpleBoundCommandGroup.SYSTEM, handler.getCommandGroup());
		assertEquals("my.resource.key", handler.getResourceKey(null).getKey());
		assertNull(handler.getConfirmKey(null, Collections.emptyMap()));
		assertEquals(ExecutableState.NOT_EXEC_HIDDEN,
			handler.isExecutable(null, null, Collections.<String, Object> emptyMap()));
		assertEquals(
			ExecutableState.NOT_EXEC_HIDDEN,
			handler.isExecutable(null,
				null, Collections.<String, Object> singletonMap(CHECK_CONFIGURED_VALUE, GLOBAL_VALUE_UNCONFIGURED)));
		assertEquals(ExecutableState.EXECUTABLE,
			handler.isExecutable(null, null,
				Collections.<String, Object> singletonMap(CHECK_CONFIGURED_VALUE, "localValue")));
		BasicTestCase.assertInstanceof(handler.checkScopeProvider(), TestingProviderConfigured.class);
		assertEquals("localValue", ((TestingProviderConfigured) handler.checkScopeProvider()).getProviderValue());
	}

	public void testGlobalRuleConfiguration() {
		CommandHandler handler = CommandHandlerFactory.getInstance().getHandler("TestAbstractCommandHandlerGlobalRule");
		BasicTestCase.assertInstanceof(handler, TestingCommand.class);

		assertEquals(ExecutableState.NOT_EXEC_HIDDEN,
			handler.isExecutable(null, null, Collections.<String, Object> emptyMap()));
		assertEquals(ExecutableState.EXECUTABLE,
			handler.isExecutable(null,
				null, Collections.<String, Object> singletonMap(CHECK_UNCONFIGURED_VALUE, GLOBAL_VALUE_UNCONFIGURED)));
		assertEquals(ExecutableState.NOT_EXEC_HIDDEN,
			handler.isExecutable(null, null,
				Collections.<String, Object> singletonMap(CHECK_UNCONFIGURED_VALUE, "localValue")));
	}

	public void testLocalRuleConfiguration() {
		CommandHandler handler = CommandHandlerFactory.getInstance().getHandler("TestAbstractCommandHandlerInlineRule");
		BasicTestCase.assertInstanceof(handler, TestingCommand.class);

		assertEquals(ExecutableState.NOT_EXEC_HIDDEN,
			handler.isExecutable(null, null, Collections.<String, Object> emptyMap()));
		assertEquals(ExecutableState.NOT_EXEC_HIDDEN,
			handler.isExecutable(null,
				null, Collections.<String, Object> singletonMap(CHECK_CONFIGURED_VALUE, GLOBAL_VALUE_UNCONFIGURED)));
		assertEquals(ExecutableState.EXECUTABLE,
			handler.isExecutable(null, null,
				Collections.<String, Object> singletonMap(CHECK_CONFIGURED_VALUE, "localValue")));
	}

	public void testLocalRuleNotConfigured() {
		CommandHandler handler =
			CommandHandlerFactory.getInstance().getHandler("TestAbstractCommandHandlerInlineRuleNotConfigured");
		BasicTestCase.assertInstanceof(handler, TestingCommand.class);

		assertEquals(TestingProviderNotConfigured.INSTANCE, handler.checkScopeProvider());
	}

	public void testModifyCommandModel() throws ConfigurationException {
		CommandHandler handler =
			CommandHandlerFactory.getInstance().getHandler("TestAbstractCommandHandlerModifyCommandModel");
		assertInstanceof(handler, TestingCommandChangeModel.class);
		LayoutComponent comp =
			new SimpleComponent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				TypedConfiguration.newConfigItem(SimpleComponent.Config.class));
		CommandModel commandModel = CommandModelFactory.commandModel(handler, comp);
		ResKey origLabel = ResKey.text("label");
		assertNotEquals(origLabel, TestingCommandChangeModel.CHANGED_LABEL);
		commandModel.setLabel(origLabel);
		assertEquals(origLabel, commandModel.getLabel());
		commandModel.executeCommand(DefaultDisplayContext.getDisplayContext());
		assertEquals(TestingCommandChangeModel.CHANGED_LABEL, commandModel.getLabel());
	}

	public static Test suite() {
		return suite(TestAbstractCommandHandler.class);
	}

}
