/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigControlProvider;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ModelReferenceChecker;

/**
 * Edits a TL-Script valued configuration property in the TL-Script editor, the one of the script
 * console, instead of a plain text field.
 *
 * <p>
 * A property whose value type is mapped to a control gets a field model holding the typed value,
 * the expression. The editor works on the source text of the script, so the two are converted
 * here, through the same format the configuration file uses: exactly the scripts a configuration
 * accepts can be entered.
 * </p>
 *
 * <p>
 * While the user types, a script is mostly incomplete. An edit is therefore stored only if it
 * parses and compiles against the application - a script referring to a variable, function or
 * model element that does not exist is rejected as well, as by {@link ModelReferenceChecker}; an
 * edit that does not is kept back and reported on the field when the user leaves the editor, so
 * that the error appears then and not on every key stroke. The stored expression stays the last
 * one accepted. The editor marks syntax errors in the text itself all along.
 * </p>
 */
public class TLScriptConfigControlProvider implements ConfigControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, ConfigFieldModel model) {
		TLScriptEditorReactControl control =
			new TLScriptEditorReactControl(context, source(model.getValue()), !model.isEditable(), List.of());
		ResKey[] pendingError = { null };
		control.setValueCallback(text -> {
			if (text == null || text.isBlank()) {
				pendingError[0] = null;
				store(model, null);
				return;
			}
			try {
				Expr expr = ExprFormat.INSTANCE.getValue(TLScriptConfigControlProvider.class.getName(), text);
				// A script that parses may still refer to a variable, function or model element
				// that does not exist; the application could not compile it then.
				ModelReferenceChecker.checkModelElements(expr);
				pendingError[0] = null;
				store(model, expr);
			} catch (ConfigurationException ex) {
				pendingError[0] = ex.getErrorKey();
			} catch (I18NRuntimeException ex) {
				pendingError[0] = ex.getErrorKey();
			}
		});
		control.setBlurCallback(() -> {
			if (pendingError[0] != null) {
				// An input error, like the one a text field reports for a value its format rejects: it
				// is shown at once, not only after the form was submitted.
				model.setError(pendingError[0]);
			}
		});

		FieldModelListener listener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				control.setValue(source(newValue));
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				control.setReadOnly(!editable);
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// The field shows the error of the model; the editor reports its own parse failures.
			}
		};
		model.addListener(listener);
		control.addCleanupAction(() -> model.removeListener(listener));

		return control;
	}

	/**
	 * Stores the given expression and drops an error reported for a script that did not parse.
	 */
	private static void store(ConfigFieldModel model, Expr expr) {
		model.setValue(expr);
		model.setError(null);
	}

	/**
	 * The source text of the given expression, empty if there is none.
	 */
	private static String source(Object value) {
		return value == null ? "" : ExprFormat.INSTANCE.getSpecification((Expr) value);
	}

}
