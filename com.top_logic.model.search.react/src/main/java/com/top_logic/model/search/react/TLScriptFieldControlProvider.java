/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Edits a TL-Script valued field in a TL-Script editor.
 *
 * <p>
 * The field holds the parsed expression, while the editor works on its source text. The conversion
 * between the two happens here, through the same format the configuration file uses, so exactly the
 * scripts a configuration accepts can be entered. A script that does not parse is reported on the
 * field and leaves the stored expression untouched.
 * </p>
 */
public class TLScriptFieldControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		TLScriptEditorReactControl control =
			new TLScriptEditorReactControl(context, source(model.getValue()), !field.isEditable(), List.of());

		if (field.isEditable()) {
			control.setValueCallback(text -> store(model, text));
		}

		FieldModelListener listener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				control.setValue(source(newValue));
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// The editor is created for the state the field has; a later change is not reflected.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// The editor reports its own parse failures.
			}
		};
		model.addListener(listener);
		control.addCleanupAction(() -> model.removeListener(listener));

		return control;
	}

	/**
	 * The source text of the given expression, empty if there is none.
	 */
	private static String source(Object value) {
		return value == null ? "" : ExprFormat.INSTANCE.getSpecification((Expr) value);
	}

	/**
	 * Parses the entered text and stores the expression it describes.
	 */
	private static void store(FieldModel model, String text) {
		if (text == null || text.isBlank()) {
			model.setValue(null);
			model.setModelValidationError(null);
			return;
		}
		try {
			Expr expr = ExprFormat.INSTANCE.getValue(TLScriptFieldControlProvider.class.getName(), text);
			model.setValue(expr);
			model.setModelValidationError(null);
		} catch (ConfigurationException ex) {
			model.setModelValidationError(ex.getErrorKey());
		}
	}

}
