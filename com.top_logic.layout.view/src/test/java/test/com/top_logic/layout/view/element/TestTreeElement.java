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
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.controlprovider.MetaResourceControlProvider;
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
	 * Tests that a node of a tree displays the object it stands for without leading away from the
	 * tree, and that a tree saying nothing about its node display gets exactly that.
	 */
	public void testNodeContent() throws Exception {
		assertNodeDisplay("A tree displays its nodes this way unless it says otherwise.",
			TypedConfiguration.newConfigItem(TreeElement.Config.class));

		assertNodeDisplay("The test tree says nothing about its node display.", readTreeConfig());
	}

	/**
	 * Tests that a display of an object leads to it wherever the application shows it, so that the
	 * tree is the only place dropping that link.
	 */
	public void testAConfiguredNodeContentLeadsToItsObject() {
		assertTrue("A display of an object leads to the place the application shows it at.",
			TypedConfiguration.newConfigItem(MetaResourceControlProvider.Config.class).getLink());
	}

	private void assertNodeDisplay(String message, TreeElement.Config treeConfig) {
		PolymorphicConfiguration<?> nodeContent = treeConfig.getNodeContent();

		assertNotNull(message, nodeContent);
		assertTrue(message + " Node content: " + nodeContent, nodeContent instanceof TreeElement.NodeDisplay);

		TreeElement.NodeDisplay display = (TreeElement.NodeDisplay) nodeContent;
		assertTrue("A node shows the icon of its object.", display.getImage());
		assertTrue("A node shows the label of its object.", display.getLabel());
		assertFalse("A click on a node selects it instead of leading away from the tree.",
			display.getLink());
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
