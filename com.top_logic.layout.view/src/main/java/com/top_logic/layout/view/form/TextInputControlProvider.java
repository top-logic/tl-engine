/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.react.control.form.InputType;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.field.FieldControlRegistry;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ReactFieldControlProvider} for string, tristate, binary, and other text-representable
 * attributes.
 *
 * <p>
 * The {@link Config#getInputType() input type} says what the entered text means: a plain text, a
 * web address, an e-mail address, a phone number, or a search term. The browser edits the value
 * with the input of that kind - offering the matching on-screen keyboard and completion - and the
 * field displays an address as a link opening it. Configured for a model type in the
 * {@link FieldControlService} type map, the input type reaches every attribute of that type.
 * </p>
 *
 * <p>
 * Also serves as the ultimate fallback when no other provider matches, where the text is a plain
 * one.
 * </p>
 *
 * <p>
 * With {@link Config#getSuggestions() suggestions}, the field offers a list of suggestions while
 * the user types, computed by a TL-Script function, see {@link ScriptSuggestionSource}.
 * </p>
 *
 * @implNote A text displayed on {@link FieldControlRegistry#isMultiline(FieldSpec) several rows}
 *           is {@link #isLarge(FieldSpec) large}: where it has no room, its first line stands for
 *           it.
 */
@Label("Text input")
@InApp
@TargetType(TLTypeKind.STRING)
public class TextInputControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration options for {@link TextInputControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<TextInputControlProvider> {

		/** Configuration name for {@link #getInputType()}. */
		String INPUT_TYPE = "input-type";

		/** The {@link #getInputType()} of a field that states none. */
		String DEFAULT_INPUT_TYPE = "TEXT";

		/** Configuration name for {@link #getSuggestions()}. */
		String SUGGESTIONS = "suggestions";

		@Override
		@ClassDefault(TextInputControlProvider.class)
		Class<? extends TextInputControlProvider> getImplementationClass();

		/**
		 * The kind of value the field edits.
		 *
		 * <p>
		 * A plain text is edited in a text box, while a web address, an e-mail address and a phone
		 * number are also offered as a link that opens the value.
		 * </p>
		 */
		@Name(INPUT_TYPE)
		@FormattedDefault(DEFAULT_INPUT_TYPE)
		InputType getInputType();

		/**
		 * Function computing the suggestions offered while the user types.
		 *
		 * <p>
		 * The function has the signature {@code self -> text -> caret -> entries}: {@code self} is
		 * the edited object (empty for a field editing no attribute of an object), {@code text} the
		 * text typed so far, and {@code caret} the position of the caret in it, counted in
		 * characters from the start.
		 * </p>
		 *
		 * <p>
		 * Each entry of the result is a suggestion, either a dictionary
		 * {@code {"label": ..., "text": ..., "from": ..., "to": ...}} - the text replacing the
		 * characters from position {@code from} up to (excluding) position {@code to} of the typed
		 * text, shown by its label - or a plain string, which is inserted at the caret. The label
		 * defaults to the text, the positions default to the caret. A malformed entry is skipped.
		 * </p>
		 *
		 * <p>
		 * The function decides which suggestions fit what is typed: its entries are offered as
		 * they are, in their order. Without a function, the field offers no suggestions.
		 * </p>
		 */
		@Name(SUGGESTIONS)
		@Nullable
		Expr getSuggestions();
	}

	private final InputType _inputType;

	private final QueryExecutor _suggestions;

	/**
	 * Creates a configured {@link TextInputControlProvider}.
	 */
	@CalledByReflection
	public TextInputControlProvider(InstantiationContext context, Config config) {
		_inputType = config.getInputType();
		_suggestions = QueryExecutor.compileOptional(config.getSuggestions());
	}

	/**
	 * The kind of value the fields built here edit.
	 */
	public InputType getInputType() {
		return _inputType;
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		ReactTextInputControl control = new ReactTextInputControl(context, model);
		if (_inputType != InputType.TEXT) {
			// A field that states no kind edits a plain text, so a plain text field is left as it
			// is built.
			control.setInputType(_inputType);
		}
		if (_suggestions != null) {
			control.setSuggestionSource(new ScriptSuggestionSource(_suggestions, model));
		}
		int rows = field.getMultilineRows();
		if (rows > 0) {
			control.setMultiline(rows);
			control.setPreformatted(field.isPreformatted());
		}
		return control;
	}

	@Override
	public boolean isLarge(FieldSpec field) {
		return FieldControlRegistry.isMultiline(field);
	}
}
