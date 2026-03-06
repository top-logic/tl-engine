/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Collections;
import java.util.List;
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
import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that {@link PanelElement} correctly parses a {@code <commands>} section.
 */
public class TestPanelCommands extends TestCase {

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
		public HandlerResult execute(ViewDisplayContext context, Object input) {
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Tests that a panel with a commands section can be parsed from XML.
	 */
	public void testParsePanelWithCommands() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestPanelCommands.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestPanelCommands.class,
			"test-panel-commands.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);
		assertEquals("View should have one content element", 1, config.getContent().size());

		assertTrue("Content should be PanelElement config",
			config.getContent().get(0) instanceof PanelElement.Config);
		PanelElement.Config panelConfig = (PanelElement.Config) config.getContent().get(0);

		// Verify commands.
		List<PolymorphicConfiguration<? extends ViewCommand>> commands = panelConfig.getCommands();
		assertEquals("Panel should have one command", 1, commands.size());

		PolymorphicConfiguration<? extends ViewCommand> cmdConfig = commands.get(0);
		assertTrue("Command should be a ViewCommand.Config",
			cmdConfig instanceof ViewCommand.Config);
		assertEquals("Command name", "testCmd", ((ViewCommand.Config) cmdConfig).getName());

		// Instantiate UIElement tree.
		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestPanelCommands.class, TypeIndex.Module.INSTANCE);
	}
}
