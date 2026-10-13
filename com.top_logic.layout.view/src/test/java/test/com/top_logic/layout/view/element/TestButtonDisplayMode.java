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

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.view.command.TestButtonCommand;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ButtonState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.ButtonElement;

/**
 * Tests that a {@code <button>} shows its command in the display mode configured for the command
 * (see {@link ViewCommand.Config}).
 */
public class TestButtonDisplayMode extends TestCase {

	/** Name of the command class the buttons under test execute. */
	private static final String COMMAND_CLASS = TestButtonCommand.TestCommand.class.getName();

	/**
	 * Tests that a command with an image that requests icon-only display is shown as icon only.
	 */
	public void testIconOnly() throws ConfigurationException {
		assertEquals(ButtonDisplayMode.ICON_ONLY.getExternalName(),
			displayMode("<action class='" + COMMAND_CLASS + "' image='css:bi bi-x' display='icon-only'/>"));
	}

	/**
	 * Tests that a command with an image that requests label-only display is shown as label only.
	 */
	public void testLabelOnly() throws ConfigurationException {
		assertEquals(ButtonDisplayMode.LABEL_ONLY.getExternalName(),
			displayMode("<action class='" + COMMAND_CLASS + "' image='css:bi bi-x' display='label-only'/>"));
	}

	/**
	 * Tests that a command with an image that requests no display mode shows icon and label.
	 */
	public void testImageWithoutMode() throws ConfigurationException {
		assertEquals(ButtonDisplayMode.ICON_LABEL.getExternalName(),
			displayMode("<action class='" + COMMAND_CLASS + "' image='css:bi bi-x'/>"));
	}

	/**
	 * Tests that a command without image that requests no display mode leaves the mode to the
	 * client, which shows the label.
	 */
	public void testNoImageWithoutMode() throws ConfigurationException {
		assertNull(displayMode("<action class='" + COMMAND_CLASS + "'/>"));
	}

	/**
	 * The display mode the control of a {@code <button>} with the given action publishes to the
	 * client.
	 */
	private static Object displayMode(String action) throws ConfigurationException {
		UIElement button = element("<button>" + action + "</button>");
		ViewContext viewContext = new DefaultViewContext(new DefaultReactContext("", "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		ReactControl control = (ReactControl) button.createControl(viewContext);
		return state(control).get(ButtonState.DISPLAY_MODE__PROP);
	}

	private static UIElement element(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestButtonDisplayMode.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"button", TypedConfiguration.getConfigurationDescriptor(ButtonElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml, "test-button.view.xml"));
		ButtonElement.Config config = (ButtonElement.Config) reader.read();
		context.checkErrors();

		UIElement result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	private static Map<?, ?> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags, and the
	 * services the command model of a button needs for its label and clique.
	 */
	public static Test suite() throws ModuleException {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestButtonDisplayMode.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE));
	}

}
