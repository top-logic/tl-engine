/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

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
import com.top_logic.layout.view.element.TableElement;

/**
 * Tests parsing and instantiation of {@link TableElement}.
 */
public class TestTableElement extends TestCase {

	/**
	 * Tests that a view XML with a {@code <table>} element can be parsed into configuration.
	 */
	public void testParseTableConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);

		// The content should be a TableElement config.
		assertEquals("View should have one content element", 1, config.getContent().size());
		assertTrue("Content should be TableElement config",
			config.getContent().get(0) instanceof TableElement.Config);

		TableElement.Config tableConfig = (TableElement.Config) config.getContent().get(0);

		// Verify inputs.
		assertEquals("Should have one input", 1, tableConfig.getInputs().size());
		assertEquals("Input channel name", "testInput", tableConfig.getInputs().get(0).getChannelName());

		// Verify rows expression is present (non-null).
		assertNotNull("Rows expression should be set", tableConfig.getRows());

		// Verify selection channel.
		assertNotNull("Selection should be set", tableConfig.getSelection());
		assertEquals("Selection channel name", "selectedRow", tableConfig.getSelection().getChannelName());
	}

	/**
	 * Tests that the parsed configuration can be instantiated into a UIElement tree.
	 */
	public void testInstantiateTableElement() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestTableElement.class, TypeIndex.Module.INSTANCE);
	}
}
