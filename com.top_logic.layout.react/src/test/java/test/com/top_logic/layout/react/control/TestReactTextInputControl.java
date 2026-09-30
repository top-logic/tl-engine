/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.InputType;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.TextInputState;
import com.top_logic.layout.react.state.TypingFieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that {@link ReactTextInputControl} tells the client what kind of value it edits and that a
 * {@link InputType#URL} field only takes an address a browser can follow.
 */
public class TestReactTextInputControl extends TestCase {

	/**
	 * A plain text field asks for no input type of its own, so the client edits it as text.
	 */
	public void testDefaultInputType() {
		TextControl field = control(null);

		assertEquals(InputType.TEXT, field.getInputType());
		assertNull(field.inputType());
	}

	/**
	 * The kind of value the field edits reaches the client as the HTML type of its input.
	 */
	public void testInputTypeIsPublished() {
		TextControl field = control(null);

		field.setInputType(InputType.URL);

		assertEquals(InputType.URL, field.getInputType());
		assertEquals("url", field.inputType());
		assertEquals("email", control(null, InputType.EMAIL).inputType());
		assertEquals("tel", control(null, InputType.TEL).inputType());
		assertEquals("search", control(null, InputType.SEARCH).inputType());
	}

	/**
	 * A web address is judged once, when the field is left, so the client holds a typed value back
	 * until then. A text the server does not read is sent while it is being typed.
	 */
	public void testUrlValueIsSentOnBlur() {
		assertEquals(Boolean.TRUE, control(null, InputType.URL).sendValueOnBlur());

		assertNull(control(null).sendValueOnBlur());
		assertNull(control(null, InputType.TEXT).sendValueOnBlur());
		assertNull(control(null, InputType.EMAIL).sendValueOnBlur());
		assertNull(control(null, InputType.TEL).sendValueOnBlur());
		assertNull(control(null, InputType.SEARCH).sendValueOnBlur());
	}

	/**
	 * An absolute address is stored as typed, without the white space around it.
	 */
	public void testValidUrl() {
		TextControl field = control(null, InputType.URL);

		field.type("  https://top-logic.com/docs  ");

		assertEquals("https://top-logic.com/docs", field.getFieldModel().getValue());
		assertFalse(model(field).hasError());
	}

	/**
	 * Any scheme makes an address absolute, not only the ones a web page is fetched with.
	 */
	public void testValidUrlOfOtherScheme() {
		TextControl field = control(null, InputType.URL);

		field.type("mailto:info@top-logic.com");

		assertEquals("mailto:info@top-logic.com", field.getFieldModel().getValue());
		assertFalse(model(field).hasError());
	}

	/**
	 * A bare host names no scheme, so it is no address a browser can follow: it is rejected and the
	 * stored address stays as it was.
	 */
	public void testUrlWithoutScheme() {
		TextControl field = control("https://top-logic.com", InputType.URL);

		field.type("example.com");

		assertTrue(model(field).hasError());
		assertEquals("https://top-logic.com", field.getFieldModel().getValue());
	}

	/**
	 * Text that is no address at all is rejected just the same.
	 */
	public void testMalformedUrl() {
		TextControl field = control("https://top-logic.com", InputType.URL);

		field.type("http://ex ample.com");

		assertTrue(model(field).hasError());
		assertEquals("https://top-logic.com", field.getFieldModel().getValue());
	}

	/**
	 * Clearing the field clears the address and any complaint the previous input left.
	 */
	public void testClearedUrl() {
		TextControl field = control("https://top-logic.com", InputType.URL);
		field.type("example.com");
		assertTrue(model(field).hasError());

		field.type("   ");

		assertNull(field.getFieldModel().getValue());
		assertFalse(model(field).hasError());
	}

	/**
	 * A plain text field stores what is typed, whatever it looks like.
	 */
	public void testTextIsStoredVerbatim() {
		TextControl field = control(null);

		field.type("  example.com  ");

		assertEquals("  example.com  ", field.getFieldModel().getValue());
		assertFalse(model(field).hasError());
	}

	/**
	 * An e-mail address or a phone number is a link on the client, but no value the server checks.
	 */
	public void testOtherTypesAreStoredVerbatim() {
		TextControl mail = control(null, InputType.EMAIL);
		mail.type("not an address");
		assertEquals("not an address", mail.getFieldModel().getValue());
		assertFalse(model(mail).hasError());

		TextControl phone = control(null, InputType.TEL);
		phone.type("+49 89 123456");
		assertEquals("+49 89 123456", phone.getFieldModel().getValue());
		assertFalse(model(phone).hasError());
	}

	private static AbstractFieldModel model(TextControl field) {
		return (AbstractFieldModel) field.getFieldModel();
	}

	private static TextControl control(Object value) {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		return new TextControl(context, new AbstractFieldModel(value));
	}

	private static TextControl control(Object value, InputType inputType) {
		TextControl result = control(value);
		result.setInputType(inputType);
		return result;
	}

	/**
	 * A {@link ReactTextInputControl} exposing its server-side input type state for assertions.
	 */
	private static final class TextControl extends ReactTextInputControl {

		TextControl(ReactContext context, FieldModel model) {
			super(context, model);
		}

		Object inputType() {
			return getState(TextInputState.INPUT_TYPE__PROP);
		}

		Object sendValueOnBlur() {
			return getState(TypingFieldState.SEND_VALUE_ON_BLUR__PROP);
		}

		void type(String text) {
			executeClientCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
				Map.of(FieldValueArguments.VALUE, text));
		}
	}

	/**
	 * The test suite, started with the resource bundles a rejected input needs for its message.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactTextInputControl.class, ResourcesModule.Module.INSTANCE));
	}

}
