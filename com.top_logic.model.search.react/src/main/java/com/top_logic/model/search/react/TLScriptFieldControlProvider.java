/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.persistency.attribute.expr.ExprStorageMapping;

/**
 * Edits a TL-Script valued field in a TL-Script editor.
 *
 * <p>
 * The editor works on the source text of the script, while the field holds it in one of two forms:
 * </p>
 * <ul>
 * <li>A configuration property holds the parsed {@link Expr expression}, converted by the
 * {@link ExprFormat format} the configuration file uses, so exactly the scripts a configuration
 * accepts can be entered.</li>
 * <li>An attribute of the model type {@code tl.model.search:Expr} holds the compiled
 * {@link SearchExpression}, converted by that type's storage mapping {@link ExprStorageMapping}, so
 * the entered script is the one that is stored.</li>
 * </ul>
 * <p>
 * Which form the field holds is told by its {@link FieldSpec#getValueType() value type}. A script
 * that cannot be read is reported on the field and leaves the stored value untouched.
 * </p>
 *
 * <p>
 * The editor is {@link #isLarge(FieldSpec) large}: where it has no room, the first line of the
 * script's source that holds more than white space stands for it.
 * </p>
 */
public class TLScriptFieldControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		ConfigurationValueProvider<Object> format = format(field);
		TLScriptEditorReactControl control =
			new TLScriptEditorReactControl(context, source(format, model.getValue()), !field.isEditable(),
				List.of());

		if (field.isEditable()) {
			control.setValueCallback(text -> store(format, model, text));
		}

		FieldModelListener listener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				control.setValue(source(format, newValue));
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

	@Override
	public boolean isLarge(FieldSpec field) {
		return true;
	}

	@Override
	public String previewText(FieldSpec field, Object value) {
		return ReactFieldControlProvider.firstNonBlankLine(source(format(field), value));
	}

	/**
	 * The conversion between the source text and the value of the given field.
	 */
	@SuppressWarnings("unchecked")
	private static ConfigurationValueProvider<Object> format(FieldSpec field) {
		Class<?> valueType = field.getValueType();
		ConfigurationValueProvider<?> format = valueType != null && SearchExpression.class.isAssignableFrom(valueType)
			? ExprStorageMapping.INSTANCE
			: ExprFormat.INSTANCE;
		return (ConfigurationValueProvider<Object>) format;
	}

	/**
	 * The source text of the given value, empty if there is none.
	 *
	 * <p>
	 * A function computed at runtime rather than written as a script has no source and is displayed
	 * as the empty text.
	 * </p>
	 */
	private static String source(ConfigurationValueProvider<Object> format, Object value) {
		if (value == null) {
			return "";
		}
		String source = format.getSpecification(value);
		return source == null ? "" : source;
	}

	/**
	 * Reads the entered text and stores the value it describes.
	 */
	private static void store(ConfigurationValueProvider<Object> format, FieldModel model, String text) {
		if (text == null || text.isBlank()) {
			model.setValue(null);
			model.setModelValidationError(null);
			return;
		}
		try {
			Object value = format.getValue(TLScriptFieldControlProvider.class.getName(), text);
			model.setValue(value);
			model.setModelValidationError(null);
		} catch (ConfigurationException ex) {
			model.setModelValidationError(ex.getErrorKey());
		} catch (I18NRuntimeException ex) {
			// A script that parses may still refer to something the model does not have.
			model.setModelValidationError(ex.getErrorKey());
		}
	}

}
