/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.suggest.Suggestion;
import com.top_logic.layout.react.control.suggest.SuggestionSource;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AttributeFieldModel;
import com.top_logic.layout.view.form.ScriptSuggestionSource;
import com.top_logic.layout.view.form.TextInputControlProvider;
import com.top_logic.model.search.expr.config.ExprFormat;

/**
 * Tests the {@link TextInputControlProvider.Config#getSuggestions() suggestions} of a text field,
 * computed by a TL-Script function through a {@link ScriptSuggestionSource}.
 */
public class TestTextInputSuggestions extends AbstractModelAccessTest {

	/** Label of the built field. */
	private static final String LABEL = "Rule";

	private ReactContext _reactContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * The suggestions are no matter of access rights: the edited object is read unrestricted.
	 */
	@Override
	protected boolean checksSecurity() {
		return false;
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		super.tearDown();
	}

	/**
	 * Without a function, the field offers no suggestions.
	 */
	public void testNoSuggestionsWithoutFunction() throws ConfigurationException {
		ReactTextInputControl control = control(null, new AbstractFieldModel(null));

		assertNull(control.getSuggestionSource());
	}

	/**
	 * A dictionary replaces the range it names by its text and is shown by its label.
	 */
	public void testDictionaryWithRange() throws ConfigurationException {
		SuggestionSource source = source(
			"self -> text -> caret -> [{\"label\": \"Kitchen\", \"text\": \"@\\\"Kitchen\\\"\", \"from\": 0, \"to\": $caret}]",
			new AbstractFieldModel(null));

		assertEquals(List.of(new Suggestion("Kitchen", "@\"Kitchen\"", 0, 5)), source.suggest("@\"Kit", 5));
	}

	/**
	 * A dictionary without label is shown by its text, one without positions is inserted at the
	 * caret.
	 */
	public void testDictionaryDefaults() throws ConfigurationException {
		SuggestionSource source = source(
			"self -> text -> caret -> [{\"text\": \"> 25\"}]", new AbstractFieldModel(null));

		assertEquals(List.of(Suggestion.insert("> 25", 3)), source.suggest("abcdef", 3));
	}

	/**
	 * A plain string is inserted at the caret.
	 */
	public void testPlainStringsInsertAtCaret() throws ConfigurationException {
		SuggestionSource source = source(
			"self -> text -> caret -> ['and', 'or']", new AbstractFieldModel(null));

		assertEquals(List.of(Suggestion.insert("and", 2), Suggestion.insert("or", 2)), source.suggest("x ", 2));
	}

	/**
	 * The function reads the typed text and the caret position, and does all filtering itself.
	 */
	public void testFunctionReadsTextAndCaret() throws ConfigurationException {
		SuggestionSource source = source(
			"self -> text -> caret -> ['Kitchen', 'Server room']"
				+ ".filter(s -> $s.stringStartsWith($text.subString(0, $caret)))",
			new AbstractFieldModel(null));

		assertEquals(List.of(Suggestion.insert("Kitchen", 3)), source.suggest("Kitxyz", 3));
		assertEquals(List.of(Suggestion.insert("Server room", 1)), source.suggest("S", 1));
	}

	/**
	 * The edited object is passed as first argument, read anew for every computation.
	 */
	public void testSelfIsTheEditedObject() throws ConfigurationException {
		AttributeFieldModel model = new AttributeFieldModel(_project, part(PROJECT, NAME));
		SuggestionSource source = source(
			"self -> text -> caret -> [$self.get(`" + qualified(PROJECT) + "#" + NAME + "`)]", model);

		assertEquals(List.of(Suggestion.insert("project", 0)), source.suggest("", 0));
	}

	/**
	 * A field editing no attribute of an object has no edited object.
	 */
	public void testNoSelfForValueField() throws ConfigurationException {
		SuggestionSource source = source(
			"self -> text -> caret -> if($self == null, 'none', 'some')", new AbstractFieldModel("value"));

		assertEquals(List.of(Suggestion.insert("none", 0)), source.suggest("", 0));
	}

	/**
	 * A malformed entry is skipped, the others are offered.
	 */
	public void testMalformedEntriesAreSkipped() throws ConfigurationException {
		SuggestionSource source = source(
			"self -> text -> caret -> ["
				+ "42, "
				+ "{\"label\": \"no text\"}, "
				+ "{\"text\": \"x\", \"from\": 3, \"to\": 1}, "
				+ "{\"text\": \"x\", \"from\": 0, \"to\": 99}, "
				+ "{\"text\": \"x\", \"from\": \"a\"}, "
				+ "{\"text\": \"x\", \"from\": 0.5}, "
				+ "'ok']",
			new AbstractFieldModel(null));

		assertEquals(List.of(Suggestion.insert("ok", 1)), source.suggest("ab", 1));
	}

	private SuggestionSource source(String function, FieldModel model) throws ConfigurationException {
		SuggestionSource result = control(function, model).getSuggestionSource();
		assertNotNull("A field with a suggestion function offers suggestions.", result);
		return result;
	}

	private ReactTextInputControl control(String function, FieldModel model) throws ConfigurationException {
		TextInputControlProvider.Config config = TypedConfiguration.newConfigItem(TextInputControlProvider.Config.class);
		if (function != null) {
			TypedConfigUtil.setProperty(config, TextInputControlProvider.Config.SUGGESTIONS,
				ExprFormat.INSTANCE.getValue(TextInputControlProvider.Config.SUGGESTIONS, function));
		}
		TextInputControlProvider provider =
			(TextInputControlProvider) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		ReactControl control = provider.createControl(_reactContext, FieldSpec.of(String.class, LABEL), model);
		return (ReactTextInputControl) control;
	}

	/**
	 * The suite installing the test model the edited object is an instance of.
	 */
	public static Test suite() {
		return suite(TestTextInputSuggestions.class);
	}

}
