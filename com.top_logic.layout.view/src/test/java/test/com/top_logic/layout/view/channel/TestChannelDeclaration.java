/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

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
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ValueChannelConfig;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Tests that channel declarations in {@code .view.xml} files are parsed and that channels are
 * registered on the {@link ViewContext} during control creation.
 */
public class TestChannelDeclaration extends TestCase {

	/**
	 * Tests that channel configs are parsed from the view XML.
	 */
	public void testParseChannelDeclarations() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestChannelDeclaration.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestChannelDeclaration.class,
			"test-channels.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertEquals("Should have 2 channel declarations", 2, config.getChannels().size());
		assertTrue("First channel should be ValueChannelConfig",
			config.getChannels().get(0) instanceof ValueChannelConfig);
		assertEquals("selectedItem", config.getChannels().get(0).getName());
		assertEquals("editMode", config.getChannels().get(1).getName());
	}

	/**
	 * Tests that resolving an unknown channel throws an exception.
	 */
	public void testResolveUnknownChannelFails() {
		ViewContext viewContext = new ViewContext(null);

		try {
			viewContext.resolveChannel(new ChannelRef("nonExistent"));
			fail("Should throw for unknown channel");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	/**
	 * Tests that duplicate channel registration throws an exception.
	 */
	public void testDuplicateChannelFails() {
		ViewContext viewContext = new ViewContext(null);
		viewContext.registerChannel("test", new DefaultViewChannel("test"));

		try {
			viewContext.registerChannel("test", new DefaultViewChannel("test"));
			fail("Should throw for duplicate channel");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	/**
	 * Tests basic channel registration and resolution on ViewContext.
	 */
	public void testRegisterAndResolve() {
		ViewContext viewContext = new ViewContext(null);
		ViewChannel channel = new DefaultViewChannel("myChannel");

		viewContext.registerChannel("myChannel", channel);

		ViewChannel resolved = viewContext.resolveChannel(new ChannelRef("myChannel"));
		assertSame("Should resolve to the same channel instance", channel, resolved);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestChannelDeclaration.class, TypeIndex.Module.INSTANCE);
	}
}
