/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.format.CommonMark;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactRenderedTextControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.html.ReactHtmlControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Test case for {@link ReactRenderedTextControl}, run with the application's {@link SafeHTML}
 * check active.
 */
@SuppressWarnings("javadoc")
public class TestReactRenderedTextControl extends TestCase {

	private static final int INPUT_PANE = 0;

	private static final int DISPLAY_PANE = 1;

	public void testEditableShowsTheInput() {
		AbstractFieldModel model = new AbstractFieldModel("*text*");
		ReactRenderedTextControl control = create(model, CommonMark::toHtml);
		assertEquals(INPUT_PANE, control.getActiveIndex());
	}

	public void testReadOnlyShowsTheRenderedText() {
		AbstractFieldModel model = new AbstractFieldModel("A **bold** word.");
		model.setEditable(false);
		ReactRenderedTextControl control = create(model, CommonMark::toHtml);

		assertEquals(DISPLAY_PANE, control.getActiveIndex());
		Map<String, Object> state = display(control);
		assertNull("CommonMark output must pass the HTML safety check.", state.get(ReactHtmlControl.ERROR));
		assertTrue(((String) state.get(ReactHtmlControl.HTML)).contains("<strong>bold</strong>"));
	}

	public void testSwitchesWithTheEditability() {
		AbstractFieldModel model = new AbstractFieldModel("first");
		ReactRenderedTextControl control = create(model, CommonMark::toHtml);

		model.setEditable(false);
		assertEquals(DISPLAY_PANE, control.getActiveIndex());

		model.setValue("*second*");
		assertTrue(((String) display(control).get(ReactHtmlControl.HTML)).contains("<em>second</em>"));

		model.setEditable(true);
		assertEquals(INPUT_PANE, control.getActiveIndex());
	}

	/**
	 * The client hands a form field's label on to the pane the deck shows - the control named by
	 * its active child. That child must be the control actually displayed: the input while the
	 * field is editable, the display while it is not.
	 */
	public void testTheShownPaneIsTheActiveChild() throws IOException {
		AbstractFieldModel model = new AbstractFieldModel("text");
		ReactRenderedTextControl control = create(model, CommonMark::toHtml);
		control.write(new TagWriter());

		assertEquals(List.of(control.getInput()), control.displayedChildren());
		assertTrue(control.stateAsJSON().contains(control.getInput().getID()));

		model.setEditable(false);
		assertEquals(List.of(control.getDisplay()), control.displayedChildren());
		assertTrue(control.stateAsJSON().contains(control.getDisplay().getID()));

		model.setEditable(true);
		assertEquals(List.of(control.getInput()), control.displayedChildren());
	}

	public void testScriptIsRejectedBySafetyCheck() {
		assertRejected("<p>text</p><script>alert(1)</script>");
	}

	public void testScriptAttributeIsRejectedBySafetyCheck() {
		assertRejected("<p onclick=\"alert(1)\">text</p>");
	}

	public void testScriptLinkIsRejectedBySafetyCheck() {
		assertRejected("<a href=\"javascript:alert(1)\">link</a>");
	}

	public void testCommonMarkDropsScriptLinks() {
		String html = CommonMark.toHtml("[link](javascript:alert(1)) <script>alert(2)</script>");
		assertFalse(html, html.contains("javascript:"));
		assertFalse(html, html.contains("<script"));
	}

	private void assertRejected(String unsafeHtml) {
		AbstractFieldModel model = new AbstractFieldModel("text");
		model.setEditable(false);
		ReactRenderedTextControl control = create(model, text -> unsafeHtml);

		Map<String, Object> state = display(control);
		assertNotNull("HTML the safety check rejects must be reported: " + unsafeHtml,
			state.get(ReactHtmlControl.ERROR));
		assertEquals("HTML the safety check rejects must not be displayed.", "",
			state.get(ReactHtmlControl.HTML) == null ? "" : state.get(ReactHtmlControl.HTML));
	}

	private static Map<String, Object> display(ReactRenderedTextControl control) {
		return control.getDisplay().scriptingScalarState();
	}

	private static ReactRenderedTextControl create(AbstractFieldModel model, Function<String, String> renderer) {
		ReactContext context = createContext();
		return new ReactRenderedTextControl(context, model, () -> new ReactTextInputControl(context, model),
			renderer);
	}

	private static ReactContext createContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactRenderedTextControl.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE, SafeHTML.Module.INSTANCE));
	}

}
