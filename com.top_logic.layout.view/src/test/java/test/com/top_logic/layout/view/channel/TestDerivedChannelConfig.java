/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

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
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ChannelFactory;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedChannelConfig;
import com.top_logic.layout.view.channel.DerivedChannelFactory;
import com.top_logic.layout.view.channel.DerivedViewChannel;
import com.top_logic.layout.view.channel.ValueChannelConfig;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Tests configuration parsing and integration of derived channels.
 *
 * <p>
 * Note: Full end-to-end tests with TL-Script expression evaluation require the application
 * services (PersistencyLayer, ModelService, SearchBuilder). The pure wiring behavior of
 * {@link DerivedViewChannel} is tested in {@link TestDerivedViewChannel}.
 * </p>
 */
public class TestDerivedChannelConfig extends TestCase {

	/**
	 * Tests that a {@code <derived-channel>} element is parsed as {@link DerivedChannelConfig}.
	 */
	public void testParseDerivedChannelConfig() throws Exception {
		ViewElement.Config config = parseTestView();

		assertEquals("Should have 2 channel declarations", 2, config.getChannels().size());

		ChannelConfig first = config.getChannels().get(0);
		assertTrue("First should be ValueChannelConfig", first instanceof ValueChannelConfig);
		assertEquals("selectedItem", first.getName());

		ChannelConfig second = config.getChannels().get(1);
		assertTrue("Second should be DerivedChannelConfig", second instanceof DerivedChannelConfig);
		assertEquals("hasSelection", second.getName());

		DerivedChannelConfig derived = (DerivedChannelConfig) second;
		assertEquals("Should have 1 input", 1, derived.getInputs().size());
		assertEquals("selectedItem", derived.getInputs().get(0).getChannelName());
		assertNotNull("Expression should be parsed", derived.getExpr());
	}

	/**
	 * Tests that {@link DerivedChannelFactory} is instantiated from parsed configuration.
	 */
	public void testDerivedChannelFactoryInstantiated() throws Exception {
		ViewElement.Config config = parseTestView();

		DefaultInstantiationContext instContext =
			new DefaultInstantiationContext(TestDerivedChannelConfig.class);

		// Instantiate the factory from the derived-channel config.
		DerivedChannelConfig derivedConfig = (DerivedChannelConfig) config.getChannels().get(1);
		ChannelFactory factory = instContext.getInstance(derivedConfig);
		instContext.checkErrors();

		assertTrue("Factory should be DerivedChannelFactory", factory instanceof DerivedChannelFactory);
	}

	/**
	 * Tests that ViewElement instantiation succeeds with derived channel configurations.
	 *
	 * <p>
	 * Verifies that the ViewElement constructor correctly processes derived channel configs and
	 * creates factory entries for them.
	 * </p>
	 */
	public void testViewElementInstantiationWithDerivedChannels() throws Exception {
		ViewElement.Config config = parseTestView();

		DefaultInstantiationContext instContext =
			new DefaultInstantiationContext(TestDerivedChannelConfig.class);
		ViewElement viewElement = (ViewElement) instContext.getInstance(config);
		instContext.checkErrors();

		assertNotNull("ViewElement should be instantiated", viewElement);
	}

	/**
	 * Tests that a manually wired derived channel works when registered through a ViewContext.
	 *
	 * <p>
	 * This simulates what {@link DerivedChannelFactory#createChannel} does at runtime, but uses a
	 * Java function instead of a TL-Script expression to avoid requiring application services.
	 * </p>
	 */
	public void testManualDerivedChannelInViewContext() {
		ViewContext viewContext = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue()));

		// Register a value channel.
		DefaultViewChannel selectedItem = new DefaultViewChannel("selectedItem");
		viewContext.registerChannel("selectedItem", selectedItem);

		// Create and wire a derived channel (simulates DerivedChannelFactory.createChannel).
		DerivedViewChannel hasSelection = new DerivedViewChannel("hasSelection");
		List<ViewChannel> inputs = List.of(viewContext.resolveChannel(new ChannelRef("selectedItem")));
		hasSelection.bind(inputs, args -> args[0] != null);
		viewContext.registerChannel("hasSelection", hasSelection);

		// Verify initial value.
		assertTrue(viewContext.hasChannel("hasSelection"));
		assertEquals(Boolean.FALSE, viewContext.resolveChannel(new ChannelRef("hasSelection")).get());

		// Set the value channel and verify derived channel updates.
		selectedItem.set("something");
		assertEquals(Boolean.TRUE, viewContext.resolveChannel(new ChannelRef("hasSelection")).get());

		// Verify read-only behavior.
		try {
			viewContext.resolveChannel(new ChannelRef("hasSelection")).set(true);
			fail("Expected IllegalStateException for read-only derived channel");
		} catch (IllegalStateException expected) {
			// Expected.
		}
	}

	/**
	 * Tests that multiple input channels can be parsed from the inputs attribute.
	 */
	public void testMultipleInputsParsing() throws Exception {
		DefaultInstantiationContext context =
			new DefaultInstantiationContext(TestDerivedChannelConfig.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestDerivedChannelConfig.class,
			"test-derived-multi-input.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertEquals(3, config.getChannels().size());

		DerivedChannelConfig derived = (DerivedChannelConfig) config.getChannels().get(2);
		assertEquals("combined", derived.getName());
		assertEquals("Should have 2 inputs", 2, derived.getInputs().size());
		assertEquals("firstName", derived.getInputs().get(0).getChannelName());
		assertEquals("lastName", derived.getInputs().get(1).getChannelName());
	}

	private ViewElement.Config parseTestView() throws Exception {
		DefaultInstantiationContext context =
			new DefaultInstantiationContext(TestDerivedChannelConfig.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestDerivedChannelConfig.class,
			"test-derived-channels.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestDerivedChannelConfig.class, TypeIndex.Module.INSTANCE);
	}
}
