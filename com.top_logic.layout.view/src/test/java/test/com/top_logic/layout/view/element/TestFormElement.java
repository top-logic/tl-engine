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
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.FieldElement;
import com.top_logic.layout.view.element.FormElement;

/**
 * Tests parsing and instantiation of {@link FormElement} and {@link FieldElement}.
 */
public class TestFormElement extends TestCase {

	/**
	 * Tests that a view XML with {@code <form>} and {@code <field>} elements can be parsed into
	 * configuration.
	 */
	public void testParseFormView() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestFormElement.class, "test-form.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);

		// The content should be a FormElement config.
		assertEquals("View should have one content element", 1, config.getContent().size());
		assertTrue("Content should be FormElement config",
			config.getContent().get(0) instanceof FormElement.Config);

		FormElement.Config formConfig = (FormElement.Config) config.getContent().get(0);

		// Verify input channel ref.
		assertNotNull("Input should be set", formConfig.getInput());
		assertEquals("Input channel name", "selectedItem", formConfig.getInput().getChannelName());

		// Verify edit mode channel ref.
		assertNotNull("EditMode should be set", formConfig.getEditMode());
		assertEquals("EditMode channel name", "isEditing", formConfig.getEditMode().getChannelName());

		// Verify dirty channel ref.
		assertNotNull("Dirty should be set", formConfig.getDirty());
		assertEquals("Dirty channel name", "isDirty", formConfig.getDirty().getChannelName());

		// Verify child FieldElement.
		assertEquals("Form should have one child", 1, formConfig.getChildren().size());
		PolymorphicConfiguration<? extends UIElement> childConfig = formConfig.getChildren().get(0);
		assertTrue("Child should be FieldElement config", childConfig instanceof FieldElement.Config);

		FieldElement.Config fieldConfig = (FieldElement.Config) childConfig;
		assertEquals("Field attribute", "name", fieldConfig.getAttribute());
	}

	/**
	 * Tests that the parsed configuration can be instantiated into a UIElement tree.
	 */
	public void testInstantiateFormElement() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFormElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestFormElement.class, "test-form.view.xml");

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
		return ServiceTestSetup.createSetup(TestFormElement.class, TypeIndex.Module.INSTANCE);
	}
}
