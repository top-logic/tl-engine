/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.editor;

import java.io.IOException;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.knowledge.gui.layout.person.ChangePasswordComponent.ApplyPasswordCommand;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.DynamicComponentDefinition.UnsupportedFormatException;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Tests the {@link DynamicComponentDefinition}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDynamicComponentDefinition extends BasicTestCase {

	/**
	 * Simple test with an empty configuration.
	 */
	public void testEmpty() throws UnsupportedFormatException, IOException, ConfigurationException {
		DynamicComponentDefinition definition = parseTemplate("test1.xml");

		ConfigurationDescriptor descriptor = definition.descriptor();

		Config layoutConfig =
			definition.createComponentConfig(new AssertProtocol(), descriptor.factory().createNew(),
				definition.definitionFile());

		assertInstanceof(layoutConfig, SimpleComponent.Config.class);
		assertEquals("", ((SimpleComponent.Config) layoutConfig).getContent());
		assertEquals(null, layoutConfig.getModelSpec());
		assertEquals(list(), layoutConfig.getButtons());

	}

	/**
	 * Simple test with an empty configuration.
	 */
	public void testParse() throws UnsupportedFormatException, IOException, ConfigurationException {
		DynamicComponentDefinition definition = parseTemplate("test1.xml");

		ConfigurationDescriptor descriptor = definition.descriptor();
		ConfigurationItem newItem = descriptor.factory().createNew();
		newItem.update(descriptor.getProperty("contentParam"), "foo");
		PropertyDescriptor modelProperty = descriptor.getProperty("model");
		Object model = modelProperty.getValueProvider().getValue("TEST",
			"selection(TestDynamicComponentDefinition_test1.xml#masterComponent)");
		newItem.update(modelProperty, model);
		List<CommandHandler.Config> buttons = createButtons();
		newItem.update(descriptor.getProperty("buttons"), buttons);

		Config layoutConfig =
			definition.createComponentConfig(new AssertProtocol(), newItem, definition.definitionFile());

		assertInstanceof(layoutConfig, SimpleComponent.Config.class);
		assertEquals("foo", ((SimpleComponent.Config) layoutConfig).getContent());
		assertEquals(model, layoutConfig.getModelSpec());
		assertConfigEquals(buttons, layoutConfig.getButtons());
	}

	private List<CommandHandler.Config> createButtons() {
		CommandHandler.Config button1 = AbstractCommandHandler.createConfig(EditComponent.CancelCommand.class, "cmd1");
		CommandHandler.Config button2 = AbstractCommandHandler.createConfig(ApplyPasswordCommand.class, "cmd2");
		List<CommandHandler.Config> buttons = list(button1, button2);
		return buttons;
	}

	/**
	 * Tests that {@link DynamicComponentDefinition} works with "include" element as component
	 * definition.
	 */
	public void testInclude() throws UnsupportedFormatException, IOException, ConfigurationException {
		DynamicComponentDefinition definition = parseTemplate("testInclude.xml");

		ConfigurationDescriptor descriptor = definition.descriptor();
		ConfigurationItem newItem = descriptor.factory().createNew();
		newItem.update(descriptor.getProperty("contentParam"), "foo");
		PropertyDescriptor modelProperty = descriptor.getProperty("model");
		Object model = modelProperty.getValueProvider().getValue("TEST",
			"selection(TestDynamicComponentDefinition_testInclude.xml#masterComponent)");
		newItem.update(modelProperty, model);
		List<CommandHandler.Config> buttons = createButtons();
		newItem.update(descriptor.getProperty("buttons"), buttons);
		Object controlProvider = newItem.value(descriptor.getProperty("componentControlProvider"));
		assertNotNull("Test expects that there is a default value.", controlProvider);

		Config layoutConfig =
			definition.createComponentConfig(new AssertProtocol(), newItem, definition.definitionFile());

		assertInstanceof(layoutConfig, SimpleComponent.Config.class);
		assertEquals("foo", ((SimpleComponent.Config) layoutConfig).getContent());
		assertEquals(model, layoutConfig.getModelSpec());
		assertEquals(controlProvider, layoutConfig.getComponentControlProvider());
		assertConfigEquals(buttons, layoutConfig.getButtons());
	}

	private DynamicComponentDefinition parseTemplate(String layoutName)
			throws IOException, UnsupportedFormatException {
		BinaryData templateData = FileManager.getInstance().getData(qualifyLayout(layoutName));
		return DynamicComponentDefinition.parseTemplate(new AssertProtocol(), templateData);
	}

	static String qualifyLayout(String layoutName) {
		return ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX + TestDynamicComponentDefinition.class.getName() + '/'
				+ layoutName;
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestDynamicComponentDefinition}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDynamicComponentDefinition.class);
	}

}
