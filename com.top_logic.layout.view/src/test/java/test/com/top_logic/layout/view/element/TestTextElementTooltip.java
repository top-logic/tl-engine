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

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.TextState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.TextElement;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Tests the {@link TextElement.Config#getTooltip() tooltip} of a {@code <text>}: computed from the
 * input value and recomputed when the channel takes another value, constant without an input, and
 * absent where the expression computes nothing.
 */
public class TestTextElementTooltip extends BasicTestCase {

	/** The tag a text is read from. */
	private static final String TEXT = "text";

	/** The channel the displayed value is held on. */
	private static final String CHANNEL = "state";

	/** The key the client asks a text for its tooltip with. */
	private static final String TOOLTIP_KEY = "tooltip";

	private DefaultViewChannel _channel;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_channel = new DefaultViewChannel(CHANNEL);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(CHANNEL, _channel);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_channel = null;
		super.tearDown();
	}

	/** The tooltip is computed from the input value, which it sees under the name given with "as". */
	public void testTooltipFromInput() throws Exception {
		_channel.set("open");
		ReactTextControl control = control("input='state as s' tooltip=\"'State: ' + $s\"");

		assertEquals(Boolean.TRUE, state(control).get(TextState.HAS_TOOLTIP__PROP));
		assertEquals("State: open", tooltip(control));
	}

	/** The input value is seen under the name of the channel when no other name is given. */
	public void testTooltipSeesChannelName() throws Exception {
		_channel.set("open");
		ReactTextControl control = control("input='state' tooltip=\"'State: ' + $state\"");

		assertEquals("State: open", tooltip(control));
	}

	/** A new channel value recomputes the tooltip together with the text. */
	public void testTooltipFollowsTheChannel() throws Exception {
		_channel.set("open");
		ReactTextControl control = control("input='state as s' tooltip=\"'State: ' + $s\"");

		_channel.set("closed");

		assertEquals("closed", control.getText());
		assertEquals("State: closed", tooltip(control));
	}

	/** The tooltip is HTML: markup in the computed text is shown as text, not interpreted. */
	public void testTooltipIsEncoded() throws Exception {
		_channel.set("<b>");
		ReactTextControl control = control("input='state as s' tooltip='$s'");

		assertEquals("&lt;b&gt;", tooltip(control));
	}

	/** Without an input, the tooltip is a constant expression. */
	public void testConstantTooltip() throws Exception {
		ReactTextControl control = control("tooltip=\"'Explanation'\"");

		assertEquals(Boolean.TRUE, state(control).get(TextState.HAS_TOOLTIP__PROP));
		assertEquals("Explanation", tooltip(control));
	}

	/** An expression computing nothing or an empty text leaves the text without a tooltip. */
	public void testEmptyResultIsNoTooltip() throws Exception {
		_channel.set("none");
		ReactTextControl control =
			control("input='state as s' tooltip=\"if($s == 'none', null, if($s == 'empty', '', $s))\"");
		assertNoTooltip(control);

		_channel.set("shown");
		assertEquals("shown", tooltip(control));

		_channel.set("empty");
		assertNoTooltip(control);

		ReactTextControl constant = control("tooltip=\"''\"");
		assertNoTooltip(constant);
	}

	/** A text without a tooltip expression has no tooltip. */
	public void testNoTooltipConfigured() throws Exception {
		_channel.set("open");
		assertNoTooltip(control("input='state'"));
	}

	/** A tooltip declaring a parameter is a configuration error: it sees the value as a variable. */
	public void testParameterIsRejected() {
		try {
			control("input='state' tooltip='v -> $v'");
			fail("A tooltip declaring a parameter must be rejected.");
		} catch (ConfigurationError | ConfigurationException | AbortExecutionException ex) {
			// Expected.
		}
	}

	private static void assertNoTooltip(ReactTextControl control) {
		assertNotSame(Boolean.TRUE, state(control).get(TextState.HAS_TOOLTIP__PROP));
		assertNull(control.getTooltipContent(TOOLTIP_KEY));
	}

	private static String tooltip(ReactTextControl control) {
		TooltipContent content = control.getTooltipContent(TOOLTIP_KEY);
		assertNotNull("The text has a tooltip.", content);
		return content.getHtml();
	}

	/** The control of a {@code <text>} with the given attributes. */
	private ReactTextControl control(String attributes) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTextElementTooltip.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(TEXT,
			TypedConfiguration.getConfigurationDescriptor(TextElement.Config.class)));
		reader.setSource(CharacterContents.newContent("<" + TEXT + " " + attributes + "/>"));
		TextElement.Config config = (TextElement.Config) reader.read();
		TextElement element = (TextElement) context.getInstance(config);
		context.checkErrors();
		return (ReactTextControl) element.createControl(_context);
	}

	/** The state of the given control as the client receives it. */
	private static Map<?, ?> state(ReactTextControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The expressions are compiled and evaluated against the application model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTextElementTooltip.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE));
	}

}
