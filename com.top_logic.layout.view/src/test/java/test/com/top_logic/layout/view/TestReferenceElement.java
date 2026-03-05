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

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ReferenceElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ChannelRef;

/**
 * Tests for {@link ReferenceElement} - the {@code <view-ref>} element.
 */
public class TestReferenceElement extends TestCase {

	/**
	 * Tests that a view-ref element with bindings is parsed from XML.
	 */
	public void testParseViewRef() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestReferenceElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestReferenceElement.class,
			"test-view-ref.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		// The content should be a ReferenceElement.
		assertEquals("Should have one content element", 1, config.getContent().size());
		assertTrue("Content should be ReferenceElement config",
			config.getContent().get(0) instanceof ReferenceElement.Config);

		ReferenceElement.Config refConfig = (ReferenceElement.Config) config.getContent().get(0);
		assertEquals("View path", "child-view.view.xml", refConfig.getView());

		// Verify bindings.
		assertEquals("Should have one binding", 1, refConfig.getBindings().size());
		ChannelBindingConfig binding = refConfig.getBindings().get(0);
		assertEquals("Binding child channel", "item", binding.getChannel());
		assertEquals("Binding parent ref", new ChannelRef("parentChannel"), binding.getTo());

		// Verify instantiation.
		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement tree should be instantiated", element);
	}

	/**
	 * Tests that a view-ref without bindings is parsed.
	 */
	public void testParseViewRefNoBindings() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestReferenceElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestReferenceElement.class,
			"test-view-ref-no-bindings.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		ReferenceElement.Config refConfig = (ReferenceElement.Config) config.getContent().get(0);
		assertEquals("View path", "child-view.view.xml", refConfig.getView());
		assertTrue("Should have no bindings", refConfig.getBindings().isEmpty());
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestReferenceElement.class, TypeIndex.Module.INSTANCE);
	}
}
