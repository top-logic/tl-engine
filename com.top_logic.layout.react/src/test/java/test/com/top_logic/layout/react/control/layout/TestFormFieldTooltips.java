/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.layout;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests what a {@link ReactFormFieldChromeControl} tells the client about the text its label says
 * over and above its own words: the description of the field, and the rich content the server
 * holds for it.
 *
 * <p>
 * Where that text is offered is the client's decision, so the control's part is to send a
 * description that exists and none that does not, and to send both texts when both are given - the
 * client then prefers the rich one.
 * </p>
 */
public class TestFormFieldTooltips extends TestCase {

	/** State key of the plain text offered as the label's tooltip. */
	private static final String TOOLTIP_TEXT = "tooltipText";

	/** State key telling the client that rich content is available under {@link #TOOLTIP_KEY}. */
	private static final String HAS_TOOLTIP = "hasTooltip";

	/** The key under which the control answers its rich tooltip content. */
	private static final String TOOLTIP_KEY = "tooltip";

	/** The description of the field under test. */
	private static final String DESCRIPTION = "What this field holds";

	/** The rich content of the field under test. */
	private static final String HTML = "<p>What this field holds, at length.</p>";

	/** A chrome exposing what it pushed to the client. */
	private static final class TestChrome extends ReactFormFieldChromeControl {

		TestChrome(ReactContext context) {
			super(context, "Label", new ReactTextInputControl(context, new AbstractFieldModel("") {
				// Plain field model with default state.
			}));
		}

		Object clientState(String key) {
			return getState(key);
		}
	}

	/**
	 * Tests that a description reaches the client as plain text, so that hovering the label costs
	 * no round trip.
	 */
	public void testDescriptionIsSentAsText() {
		TestChrome chrome = chrome();

		chrome.setTooltipText(DESCRIPTION);

		assertEquals(DESCRIPTION, chrome.clientState(TOOLTIP_TEXT));
		assertFalse("No rich content is announced.", Boolean.TRUE.equals(chrome.clientState(HAS_TOOLTIP)));
	}

	/**
	 * Tests that a field without a description sends none, so that its label offers nothing.
	 */
	public void testFieldWithoutDescriptionSendsNone() {
		TestChrome chrome = chrome();

		chrome.setTooltipText(DESCRIPTION);
		chrome.setTooltipText("");

		assertNull("An empty description is no description.", chrome.clientState(TOOLTIP_TEXT));
	}

	/**
	 * Tests that a field carrying both texts sends both: the client prefers the rich content and
	 * fetches it under the key the control answers.
	 */
	public void testRichContentIsSentBesideTheText() {
		TestChrome chrome = chrome();

		chrome.setTooltipText(DESCRIPTION);
		chrome.setTooltip(HTML, null);

		assertEquals(Boolean.TRUE, chrome.clientState(HAS_TOOLTIP));
		assertEquals(DESCRIPTION, chrome.clientState(TOOLTIP_TEXT));
		TooltipContent content = chrome.getTooltipContent(TOOLTIP_KEY);
		assertNotNull("The control answers its own key.", content);
		assertEquals(HTML, content.getHtml());
	}

	/** A chrome over a plain text field. */
	private static TestChrome chrome() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		return new TestChrome(context);
	}

	/** Suite requiring the theme the chrome takes its validation icons from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestFormFieldTooltips.class, ThemeFactory.Module.INSTANCE));
	}

}
