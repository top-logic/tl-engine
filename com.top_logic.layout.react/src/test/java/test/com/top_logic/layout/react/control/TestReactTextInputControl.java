/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.top_logic.layout.react.control.suggest.SuggestArguments;
import com.top_logic.layout.react.control.suggest.Suggestion;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.SuggestionEntry;
import com.top_logic.layout.react.state.TextInputState;
import com.top_logic.layout.react.state.TypingFieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that {@link ReactTextInputControl} tells the client what kind of value it edits, that a
 * {@link InputType#URL} field only takes an address a browser can follow, and that a field with a
 * {@link ReactTextInputControl#setSuggestionSource(com.top_logic.layout.react.control.suggest.SuggestionSource)
 * suggestion source} offers the suggestions it computes.
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

	/**
	 * A field without a suggestion source says nothing about suggestions, and asking it for some
	 * leaves its state alone.
	 */
	public void testNoSuggestionsWithoutSource() {
		TextControl field = control("Kitchen");

		field.suggest("@\"Kit", 5);

		assertNull(field.getState(TextInputState.OFFERS_SUGGESTIONS__PROP));
		assertNull(field.getState(TextInputState.SUGGESTIONS__PROP));
		assertNull(field.getState(TextInputState.SUGGESTIONS_TEXT__PROP));
		assertNull(field.getState(TextInputState.SUGGESTIONS_CARET__PROP));
	}

	/**
	 * The suggestions of the source reach the client in the order computed, together with the text
	 * and caret position they are computed for.
	 */
	public void testSuggestionsArePublished() {
		TextControl field = control(null);
		List<Object> asked = new ArrayList<>();
		field.setSuggestionSource((text, caret) -> {
			asked.add(text);
			asked.add(caret);
			return List.of(
				new Suggestion("Kitchen sensor", "@\"Kitchen\"", 0, text.length()),
				Suggestion.insert(" > ", caret));
		});
		assertEquals(Boolean.TRUE, field.getState(TextInputState.OFFERS_SUGGESTIONS__PROP));

		field.suggest("@\"Kit", 5);

		assertEquals(List.of("@\"Kit", 5), asked);
		assertEquals(List.of(
			entry("Kitchen sensor", "@\"Kitchen\"", 0, 5),
			entry(" > ", " > ", 5, 5)),
			field.getState(TextInputState.SUGGESTIONS__PROP));
		assertEquals("@\"Kit", field.getState(TextInputState.SUGGESTIONS_TEXT__PROP));
		assertEquals(5, field.getState(TextInputState.SUGGESTIONS_CARET__PROP));
	}

	/**
	 * A suggestion replacing characters behind the end of the text is not offered, the others are.
	 */
	public void testSuggestionNotFittingTheTextIsDropped() {
		TextControl field = control(null);
		field.setSuggestionSource((text, caret) -> List.of(
			new Suggestion(null, "x", 0, text.length() + 1),
			Suggestion.insert("y", caret)));

		field.suggest("ab", 1);

		assertEquals(List.of(entry("y", "y", 1, 1)), field.getState(TextInputState.SUGGESTIONS__PROP));
	}

	/**
	 * Computing suggestions never changes the value of the field.
	 */
	public void testSuggestingKeepsTheValue() {
		TextControl field = control("stored");
		field.setSuggestionSource((text, caret) -> List.of(Suggestion.insert("more", caret)));

		field.suggest("typed", 5);

		assertEquals("stored", field.getFieldModel().getValue());
		assertFalse(model(field).hasError());
	}

	/**
	 * Removing the source withdraws the offer and the suggestions computed so far.
	 */
	public void testRemovedSourceWithdrawsSuggestions() {
		TextControl field = control(null);
		field.setSuggestionSource((text, caret) -> List.of(Suggestion.insert("x", caret)));
		field.suggest("a", 1);

		field.setSuggestionSource(null);

		assertNull(field.getState(TextInputState.OFFERS_SUGGESTIONS__PROP));
		assertNull(field.getState(TextInputState.SUGGESTIONS__PROP));
	}

	private static Map<String, Object> entry(String label, String text, int from, int to) {
		Map<String, Object> result = new HashMap<>();
		result.put(SuggestionEntry.LABEL__PROP, label);
		result.put(SuggestionEntry.TEXT__PROP, text);
		result.put(SuggestionEntry.FROM__PROP, from);
		result.put(SuggestionEntry.TO__PROP, to);
		return result;
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

		@Override
		public Object getState(String key) {
			return super.getState(key);
		}

		void suggest(String text, int caret) {
			executeClientCommand(SuggestArguments.COMMAND,
				Map.of(SuggestArguments.TEXT, text, SuggestArguments.CARET, caret));
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
