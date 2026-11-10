/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.LinkKind;
import com.top_logic.layout.react.control.common.ReactLinkControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.LinkState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.element.LinkDisplay;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link LinkDisplay}: the address, text and tooltip of the link it shows a value as, and
 * the state its control sends.
 */
public class TestLinkDisplay extends BasicTestCase {

	/** The tag a display is read from. */
	private static final String DISPLAY = "display";

	/** A web address displayed. */
	private static final String URL = "https://www.top-logic.com/";

	/** An e-mail address displayed. */
	private static final String MAIL = "info@top-logic.com";

	/** A value holding a web address is a link opening it in a new window, showing the address. */
	public void testUrl() throws Exception {
		LinkDisplay display = display("");

		Map<?, ?> state = state(display.createControl(context(), URL));
		assertEquals(URL, state.get(LinkState.ADDRESS__PROP));
		assertEquals(URL, state.get(LinkState.TEXT__PROP));
		assertEquals(LinkKind.URL.getExternalName(), state.get(LinkState.KIND__PROP));
		assertEquals(Boolean.FALSE, state.get(LinkState.SAME_WINDOW__PROP));
		assertNull(state.get(LinkState.TOOLTIP__PROP));
		assertEquals(URL, display.getLabel(URL));
	}

	/** An e-mail address is sent with its kind, which makes the client write an e-mail to it. */
	public void testEmail() throws Exception {
		LinkDisplay display = display("kind='email' new-window='false'");

		ReactControl control = display.createControl(context(), MAIL);
		Map<?, ?> state = state(control);
		assertEquals(MAIL, state.get(LinkState.ADDRESS__PROP));
		assertEquals("email", state.get(LinkState.KIND__PROP));
		assertEquals(Boolean.TRUE, state.get(LinkState.SAME_WINDOW__PROP));
		assertFalse(((ReactLinkControl) control).isNewWindow());
	}

	/** Address, text and tooltip are computed by their expressions. */
	public void testComputed() throws Exception {
		LinkDisplay display = display("href=\"'https://' + $value + '/'\" label=\"'Site ' + $value\""
			+ " tooltip=\"'Open ' + $value\"");

		Map<?, ?> state = state(display.createControl(context(), "top-logic.com"));
		assertEquals("https://top-logic.com/", state.get(LinkState.ADDRESS__PROP));
		assertEquals("Site top-logic.com", state.get(LinkState.TEXT__PROP));
		assertEquals("Open top-logic.com", state.get(LinkState.TOOLTIP__PROP));
		assertEquals("The search finds the cell by the text it shows.", "Site top-logic.com",
			display.getLabel("top-logic.com"));
	}

	/** A label computed as nothing shows the address. */
	public void testLabelComputedAsNothing() throws Exception {
		LinkDisplay display = display("label='null'");

		assertEquals(URL, display.getLabel(URL));
	}

	/** A value without an address is no link. */
	public void testNoAddress() throws Exception {
		LinkDisplay display = display("href='null' label=\"'No site'\"");

		Map<?, ?> state = state(display.createControl(context(), "x"));
		assertEquals("", state.get(LinkState.ADDRESS__PROP));
		assertEquals("No site", state.get(LinkState.TEXT__PROP));

		state = state(display(" ").createControl(context(), ""));
		assertEquals("", state.get(LinkState.ADDRESS__PROP));
		assertEquals("", state.get(LinkState.TEXT__PROP));
	}

	/** A link opens a web address in a new window unless configured otherwise. */
	public void testDefaults() {
		LinkDisplay.Config config = TypedConfiguration.newConfigItem(LinkDisplay.Config.class);
		assertEquals(LinkKind.URL, config.getKind());
		assertTrue(config.getNewWindow());
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/** The state of the given control as the client receives it. */
	private static Map<?, ?> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/** The display read from a {@value #DISPLAY} tag with the given attributes. */
	private static LinkDisplay display(String attributes) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestLinkDisplay.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(DISPLAY,
			TypedConfiguration.getConfigurationDescriptor(LinkDisplay.Config.class)));
		reader.setSource(CharacterContents.newContent("<" + DISPLAY + " " + attributes + "/>"));
		LinkDisplay.Config config = (LinkDisplay.Config) reader.read();
		LinkDisplay result = context.getInstance(config);
		context.checkErrors();
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The expressions are compiled and evaluated against the application model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestLinkDisplay.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE));
	}

}
