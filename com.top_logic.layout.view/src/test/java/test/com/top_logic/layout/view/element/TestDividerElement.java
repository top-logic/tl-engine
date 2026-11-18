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

import com.top_logic.basic.AbortExecutionException;
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
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactDividerControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DividerState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.DividerElement;

/**
 * Tests what a {@code <divider>} publishes to the client: each configured attribute reaches the
 * state of its control, an attribute not given takes its default, and a text on a vertical line is
 * a configuration error.
 */
public class TestDividerElement extends TestCase {

	/** Without any attribute the line is horizontal, plain, with its text in the middle, full width. */
	public void testDefaults() throws ConfigurationException {
		Map<?, ?> state = state("<divider/>");

		assertEquals("horizontal", state.get(DividerState.ORIENTATION__PROP));
		assertNull("A divider without a label shows no text.", state.get(DividerState.LABEL__PROP));
		assertEquals("center", state.get(DividerState.LABEL_POSITION__PROP));
		assertEquals("none", state.get(DividerState.INSET__PROP));
	}

	/** A vertical line reaches the client as such. */
	public void testVertical() throws ConfigurationException {
		assertEquals("vertical", state("<divider orientation='vertical'/>").get(DividerState.ORIENTATION__PROP));
	}

	/** The label is published as the text in the language of the user. */
	public void testLabel() throws ConfigurationException {
		Map<?, ?> state = state("<divider><label><en>Address</en></label></divider>");

		assertEquals("Address", state.get(DividerState.LABEL__PROP));
	}

	/** Each position of the text reaches the client. */
	public void testLabelPosition() throws ConfigurationException {
		for (ReactDividerControl.LabelPosition position : ReactDividerControl.LabelPosition.values()) {
			String name = position.getExternalName();
			assertEquals(name, state("<divider label-position='" + name + "'><label><en>Text</en></label></divider>")
				.get(DividerState.LABEL_POSITION__PROP));
		}
	}

	/** An inset line reaches the client as such. */
	public void testInset() throws ConfigurationException {
		assertEquals("start", state("<divider inset='start'/>").get(DividerState.INSET__PROP));
	}

	/** The configured class of the element reaches the control. */
	public void testCssClass() throws ConfigurationException {
		assertEquals("my-divider", state("<divider css-class='my-divider'/>").get(DividerState.CSS_CLASS__PROP));
	}

	/** Only a horizontal line carries a text. */
	public void testLabelOnVerticalIsAnError() {
		try {
			element("<divider orientation='vertical'><label><en>Text</en></label></divider>");
			fail("A text on a vertical divider must be a configuration error.");
		} catch (ConfigurationException | AbortExecutionException ex) {
			// Expected: the element reports the error to its instantiation context.
		}
	}

	private static Map<?, ?> state(String xml) throws ConfigurationException {
		ViewContext context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		ReactControl control = (ReactControl) element(xml).createControl(context);
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

	private static UIElement element(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestDividerElement.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"divider", TypedConfiguration.getConfigurationDescriptor(DividerElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml, "test-divider.view.xml"));
		DividerElement.Config config = (DividerElement.Config) reader.read();
		context.checkErrors();

		UIElement result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The label is resolved to the text displayed through the resources.
	 */
	public static Test suite() throws ModuleException {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDividerElement.class, TypeIndex.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE));
	}

}
