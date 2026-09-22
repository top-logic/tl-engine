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
import com.top_logic.table.SelectionMode;

/**
 * Tests parsing and instantiation of {@link TreeElement}.
 */
public class TestTreeElement extends TestCase {

	/**
	 * Tests that a view XML with a {@code <tree>} element can be parsed into configuration.
	 */
	public void testParseTreeConfig() throws Exception {
		TreeElement.Config treeConfig = readTreeConfig();

		// Verify inputs.
		assertEquals("Should have one input", 1, treeConfig.getInputs().size());
		assertEquals("Input channel name", "rootInput", treeConfig.getInputs().get(0).getChannelName());

		// Verify root expression is present (non-null).
		assertNotNull("Root expression should be set", treeConfig.getRoot());

		// Verify children expression is present (non-null).
		assertNotNull("Children expression should be set", treeConfig.getChildren());

		// Verify selection channel.
		assertNotNull("Selection should be set", treeConfig.getSelection());
		assertEquals("Selection channel name", "selectedNode", treeConfig.getSelection().getChannelName());
	}

	/**
	 * Tests that the function telling what holds an object in the tree is optional, and is read
	 * where the tree declares it.
	 */
	public void testParents() throws Exception {
		assertNull("A tree that does not say what holds an object is searched for a node.",
			TypedConfiguration.newConfigItem(TreeElement.Config.class).getParents());

		assertNotNull("The tree declares what holds an object in it.", readTreeConfig().getParents());
	}

	/**
	 * Tests that the parsed configuration can be instantiated into a UIElement tree.
	 */
	public void testInstantiateTreeElement() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeElement.class);

		UIElement element = context.getInstance(readViewConfig(context));
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Tests that a tree selects one node at a time unless it configures the selection of any number
	 * of them.
	 */
	public void testSelectionMode() throws Exception {
		assertEquals("A tree selects one node at a time unless it says otherwise.",
			SelectionMode.SINGLE,
			TypedConfiguration.newConfigItem(TreeElement.Config.class).getSelectionMode());

		assertEquals("The tree configures the selection of any number of nodes.",
			SelectionMode.MULTI, readTreeConfig().getSelectionMode());
	}

	/**
	 * The {@link TreeElement.Config} of the test view.
	 */
	private TreeElement.Config readTreeConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTreeElement.class);

		ViewElement.Config config = readViewConfig(context);

		context.checkErrors();
		assertNotNull("Config should be parsed", config);

		// The content should be a TreeElement config.
		assertTrue("Content should be TreeElement config",
			config.getContent() instanceof TreeElement.Config);

		return (TreeElement.Config) config.getContent();
	}

	/**
	 * Reads the test view.
	 */
	private ViewElement.Config readViewConfig(DefaultInstantiationContext context) throws Exception {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTreeElement.class, "test-tree.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		return (ViewElement.Config) reader.read();
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestTreeElement.class, TypeIndex.Module.INSTANCE);
	}
}
