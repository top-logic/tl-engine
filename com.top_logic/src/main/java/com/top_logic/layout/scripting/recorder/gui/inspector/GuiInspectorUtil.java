/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.Logger;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.DebuggingConfig;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;

/**
 * Utility methods for the gui inspector.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GuiInspectorUtil {

	public static final boolean DEFAULT_ENABLED_STATE = false;

	/** Failsafe (Catches every {@link Throwable} and returns the {@link #DEFAULT_ENABLED_STATE}.) */
	public static boolean isGuiInspectorEnabled() {
		return (failsafeIsGuiInspectorEnabled() || ScriptingRecorder.isEnabled());
	}

	private static boolean failsafeIsGuiInspectorEnabled() {
		try {
			return DebuggingConfig.configuredInstance().getInspectEnabled();
		} catch (Throwable exception) {
			// 'Throwable', to make sure a mistake in these settings don't crash the application.
			// (The features configured here are not used in productive systems at all.)
			String errorMessage = "Unable to get the configuration for the gui inspector. Section '"
					+ DebuggingConfig.class.getName() + "'; Entry: '" + DebuggingConfig.INSPECT_ENABLED + "'";
			Logger.error(errorMessage, exception, AJAXCommandHandler.class);
			return DEFAULT_ENABLED_STATE;
		}
	}

	/**
	 * Creates a field for the type of the given value.
	 * 
	 * @param value
	 *        The value to create a field for. The returned field does not have the given value as
	 *        value.
	 * @param name
	 *        The Name of the expected field.
	 * 
	 * @return A {@link FormField} appropriate to the given value type.
	 */
	public static FormField newFieldForValueType(Object value, String name) {
		FormField valueField;
		if (value instanceof String) {
			valueField = FormFactory.newStringField(name);
		} else if (value instanceof Integer) {
			valueField = FormFactory.newIntField(name);
		} else if (value instanceof Long) {
			valueField = FormFactory.newLongField(name, null, false);
		} else if (value instanceof Double) {
			valueField = FormFactory.newDoubleField(name, 0.0, false);
		} else if (value instanceof Date) {
			valueField = FormFactory.newDateField(name, new Date(), false);
		} else if (value instanceof Boolean) {
			valueField = FormFactory.newBooleanField(name);
		} else if (value instanceof List<?>) {
			valueField = FormFactory.newSelectField(name, Collections.emptyList(), FormFactory.MULTIPLE,
				Collections.emptyList(), FormFactory.IMMUTABLE);
		} else {
			valueField = FormFactory.newHiddenField(name);
			valueField.setControlProvider(new ControlProvider() {

				@Override
				public Control createControl(Object model, String style) {
					return new SimpleConstantControl<>(((FormField) model).getValue());
				}
			});
		}
		return valueField;
	}

}
