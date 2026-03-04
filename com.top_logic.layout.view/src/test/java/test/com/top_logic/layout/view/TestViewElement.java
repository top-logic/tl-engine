/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.AppShellElement;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.element.StackElement;

/**
 * Tests parsing of a {@code .view.xml} file into a {@link ViewElement.Config} and instantiating the
 * {@link UIElement} tree (Phase 1 of the two-phase lifecycle).
 */
public class TestViewElement extends TestCase {

	/**
	 * Tests that a view XML file can be parsed into configuration and instantiated into a
	 * {@link UIElement} tree.
	 */
	public void testParseAndInstantiate() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestViewElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestViewElement.class, "test-view.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);
		assertNotNull("Content should be set", config.getContent());

		// Verify the parsed structure: view -> AppShellElement -> PanelElement -> StackElement
		assertTrue("Content should be AppShellElement config",
			config.getContent() instanceof AppShellElement.Config);
		AppShellElement.Config appShellConfig = (AppShellElement.Config) config.getContent();

		assertNotNull("AppShell content should be set", appShellConfig.getContent());
		assertTrue("AppShell content should be PanelElement config",
			appShellConfig.getContent() instanceof PanelElement.Config);
		PanelElement.Config panelConfig = (PanelElement.Config) appShellConfig.getContent();
		assertEquals("Panel title", "Test Panel", panelConfig.getTitle());

		assertEquals("Panel should have one child", 1, panelConfig.getChildren().size());
		assertTrue("Panel child should be StackElement config",
			panelConfig.getChildren().get(0) instanceof StackElement.Config);
		StackElement.Config stackConfig = (StackElement.Config) panelConfig.getChildren().get(0);
		assertEquals("Stack direction", "row", stackConfig.getDirection());
		assertEquals("Stack gap", "compact", stackConfig.getGap());

		// Phase 1: Instantiate UIElement tree from configuration.
		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestViewElement.class, TypeIndex.Module.INSTANCE));
	}
}
