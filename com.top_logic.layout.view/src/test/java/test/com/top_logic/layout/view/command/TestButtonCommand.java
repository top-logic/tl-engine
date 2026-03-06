/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.ReactDisplayContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.ButtonElement;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that {@link ButtonElement} can parse and instantiate inline {@link ViewCommand}s.
 */
public class TestButtonCommand extends TestCase {

	/**
	 * A simple test command implementation.
	 */
	public static class TestCommand implements ViewCommand {

		/**
		 * Configuration for {@link TestCommand}.
		 */
		public interface Config extends ViewCommand.Config {

			@Override
			@ClassDefault(TestCommand.class)
			Class<? extends ViewCommand> getImplementationClass();
		}

		/**
		 * Creates a new {@link TestCommand}.
		 */
		@CalledByReflection
		public TestCommand(InstantiationContext context, Config config) {
			// No-op.
		}

		@Override
		public HandlerResult execute(ReactDisplayContext context, Object input) {
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Tests that a button with an inline command can be parsed from XML.
	 */
	public void testParseButtonWithCommand() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestButtonCommand.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestButtonCommand.class,
			"test-button-command.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);
		assertEquals("View should have one content element", 1, config.getContent().size());

		// The content element should be a ButtonElement.Config.
		assertTrue("Content should be ButtonElement config",
			config.getContent().get(0) instanceof ButtonElement.Config);
		ButtonElement.Config buttonConfig = (ButtonElement.Config) config.getContent().get(0);

		// The action should be a ViewCommand.Config.
		PolymorphicConfiguration<? extends ViewCommand> actionConfig = buttonConfig.getAction();
		assertNotNull("Action should be set", actionConfig);
		assertTrue("Action should be a ViewCommand.Config",
			actionConfig instanceof ViewCommand.Config);

		// Instantiate UIElement tree.
		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestButtonCommand.class, TypeIndex.Module.INSTANCE);
	}
}
