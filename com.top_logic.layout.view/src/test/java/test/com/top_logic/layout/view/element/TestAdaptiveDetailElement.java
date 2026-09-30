/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;
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
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.AdaptiveDetailElement;
import com.top_logic.layout.view.element.DetailDisplay;

/**
 * Tests parsing and instantiation of {@link AdaptiveDetailElement} and its detail presentation
 * configuration.
 */
public class TestAdaptiveDetailElement extends TestCase {

	private List<PolymorphicConfiguration<? extends UIElement>> parseTestView() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestAdaptiveDetailElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source =
			new ClassRelativeBinaryContent(TestAdaptiveDetailElement.class, "test-adaptive-detail.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertTrue("Content should be a container of the tested elements",
			config.getContent() instanceof ContainerElement.Config);
		return ((ContainerElement.Config) config.getContent()).getChildren();
	}

	private AdaptiveDetailElement.Config adaptiveDetail(int index) throws Exception {
		PolymorphicConfiguration<? extends UIElement> child = parseTestView().get(index);
		assertTrue("Child " + index + " should be an AdaptiveDetailElement config",
			child instanceof AdaptiveDetailElement.Config);
		return (AdaptiveDetailElement.Config) child;
	}

	/**
	 * Tests that an {@code <adaptive-detail>} saying nothing about its detail presentation divides
	 * the width in a split.
	 */
	public void testDetailDisplayDefaults() throws Exception {
		AdaptiveDetailElement.Config config = adaptiveDetail(0);

		assertEquals("Selection channel", "selectedSplit", config.getSelection().getChannelName());
		assertEquals("Detail display defaults to a split", DetailDisplay.SPLIT, config.getDetailDisplay());
		assertEquals("Detail size defaults to the configured default",
			AdaptiveDetailElement.Config.DEFAULT_DETAIL_SIZE, config.getDetailSize());
	}

	/**
	 * Tests that an {@code <adaptive-detail>} asking for a drawer of an explicit width parses both
	 * values.
	 */
	public void testDetailDisplayDrawer() throws Exception {
		AdaptiveDetailElement.Config config = adaptiveDetail(1);

		assertEquals("Selection channel", "selectedDrawer", config.getSelection().getChannelName());
		assertEquals("Detail display", DetailDisplay.DRAWER, config.getDetailDisplay());
		assertEquals("Detail size", 360, config.getDetailSize());
	}

	/**
	 * Tests that the parsed configuration instantiates into a {@link UIElement} tree.
	 */
	public void testInstantiateAdaptiveDetailElements() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestAdaptiveDetailElement.class);
		for (PolymorphicConfiguration<? extends UIElement> child : parseTestView()) {
			UIElement element = context.getInstance(child);
			context.checkErrors();
			assertTrue("Should be an AdaptiveDetailElement", element instanceof AdaptiveDetailElement);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestAdaptiveDetailElement.class, TypeIndex.Module.INSTANCE);
	}
}
