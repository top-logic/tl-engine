/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.list;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
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
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.list.ObjectListElement;
import com.top_logic.layout.view.list.ObjectListElement.Layout;

/**
 * Tests the configuration of an {@link ObjectListElement}: the inputs its functions are applied to,
 * and the container its elements are arranged in.
 */
public class TestObjectListConfig extends TestCase {

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * Tests that a list reads its inputs and its arrangement from the view XML.
	 */
	public void testParseObjectListConfig() throws Exception {
		ObjectListElement.Config config = readConfig("test-object-list.view.xml");

		assertEquals("The inputs are read in declaration order.",
			List.of(new ChannelRef("catalogue"), new ChannelRef("season")), config.getInputs());
		assertNotNull("The element lookup is read.", config.getItems());
		assertEquals("The configured arrangement.", Layout.GRID, config.getLayout());
		assertEquals("The configured column bound.", Integer.valueOf(3), config.getMaxColumns());
		assertEquals("The configured column width.", "18rem", config.getMinColumnWidth());
		assertEquals("The item content is read.", 1, config.getItem().size());
	}

	/**
	 * Tests that the inputs are equally read from the notation listing one element per channel.
	 */
	public void testParseNestedInputs() throws Exception {
		ObjectListElement.Config config = readConfig("test-object-list-inputs.view.xml");

		assertEquals("The inputs are read in declaration order.",
			List.of(new ChannelRef("catalogue"), new ChannelRef("season")), config.getInputs());
	}

	/**
	 * Tests that a list arranges its elements in a column unless it says otherwise.
	 */
	public void testDefaultLayout() {
		assertEquals(Layout.LIST, TypedConfiguration.newConfigItem(ObjectListElement.Config.class).getLayout());
	}

	/**
	 * Tests that a grid list arranges its elements in a grid of the configured columns, each element
	 * wrapped in an item of the list's CSS class.
	 */
	public void testGridArrangement() throws Exception {
		ObjectListElement.Config config = readConfig("test-object-list.view.xml");
		ReactLayoutControl container = config.getLayout().createContainer(_context, config);

		assertEquals("The elements are arranged by the grid component.", "TLGrid",
			container.getReactModule());
		String state = state(container);
		assertContains("The configured column width reaches the client.", "\"minColumnWidth\":\"18rem\"", state);
		assertContains("The configured column bound reaches the client.", "\"maxColumns\":3", state);
		assertContains("Every element is wrapped in an item of the list's class.",
			"\"itemClass\":\"" + ObjectListElement.ITEM_CSS_CLASS + "\"", state);
	}

	/**
	 * Tests that a list arranges its elements in a column, each element wrapped in an item of the
	 * same CSS class as in a grid.
	 */
	public void testListArrangement() throws IOException {
		ObjectListElement.Config config = TypedConfiguration.newConfigItem(ObjectListElement.Config.class);
		ReactLayoutControl container = config.getLayout().createContainer(_context, config);

		assertEquals("The elements are arranged by the stack component.", "TLStack",
			container.getReactModule());
		String state = state(container);
		assertContains("The elements are placed below each other.", "\"direction\":\"column\"", state);
		assertContains("Every element is wrapped in an item of the list's class.",
			"\"itemClass\":\"" + ObjectListElement.ITEM_CSS_CLASS + "\"", state);
	}

	/**
	 * The state the given control hands to the client.
	 */
	private static String state(ReactControl control) throws IOException {
		TagWriter out = new TagWriter();
		control.write(out);

		// The state is serialized into an HTML attribute.
		return out.toString().replace("&quot;", "\"");
	}

	private static void assertContains(String message, String expected, String actual) {
		assertTrue(message + " Expected to find '" + expected + "' in: " + actual, actual.contains(expected));
	}

	/**
	 * The {@link ObjectListElement} configuration of the given test view.
	 */
	private static ObjectListElement.Config readConfig(String viewResource) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestObjectListConfig.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestObjectListConfig.class, viewResource);

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertInstanceof("The view content is an object list.", ObjectListElement.Config.class, config.getContent());
		return (ObjectListElement.Config) config.getContent();
	}

	private static void assertInstanceof(String message, Class<?> expected, Object value) {
		assertTrue(message + " Was: " + (value == null ? null : value.getClass().getName()),
			expected.isInstance(value));
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestObjectListConfig.class, TypeIndex.Module.INSTANCE);
	}

}
