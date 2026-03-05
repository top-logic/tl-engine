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
import com.top_logic.layout.view.element.TreeElement;

/**
 * Tests parsing and instantiation of {@link TreeElement}.
 */
public class TestTreeElement extends TestCase {

	/**
	 * Tests that a view XML with a {@code <tree>} element can be parsed into configuration.
	 */
	public void testParseTreeConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTreeElement.class, "test-tree.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);

		// The content should be a TreeElement config.
		assertEquals("View should have one content element", 1, config.getContent().size());
		assertTrue("Content should be TreeElement config",
			config.getContent().get(0) instanceof TreeElement.Config);

		TreeElement.Config treeConfig = (TreeElement.Config) config.getContent().get(0);

		// Verify inputs.
		assertEquals("Should have one input", 1, treeConfig.getInputs().size());
		assertEquals("Input channel name", "rootInput", treeConfig.getInputs().get(0).getChannelName());

		// Verify root expression is present (non-null).
		assertNotNull("Root expression should be set", treeConfig.getRoot());

		// Verify children expression is present (non-null).
		assertNotNull("Children expression should be set", treeConfig.getChildren());

		// Verify isLeaf expression is present (non-null).
		assertNotNull("IsLeaf expression should be set", treeConfig.getIsLeaf());

		// Verify selection channel.
		assertNotNull("Selection should be set", treeConfig.getSelection());
		assertEquals("Selection channel name", "selectedNode", treeConfig.getSelection().getChannelName());

		// Verify optional properties are null.
		assertNull("supportsNode should be null", treeConfig.getSupportsNode());
		assertNull("modelForNode should be null", treeConfig.getModelForNode());
		assertNull("parents should be null", treeConfig.getParents());
		assertNull("nodesToUpdate should be null", treeConfig.getNodesToUpdate());
	}

	/**
	 * Tests that the parsed configuration can be instantiated into a UIElement tree.
	 */
	public void testInstantiateTreeElement() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTreeElement.class, "test-tree.view.xml");

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
		return ServiceTestSetup.createSetup(TestTreeElement.class, TypeIndex.Module.INSTANCE);
	}
}
