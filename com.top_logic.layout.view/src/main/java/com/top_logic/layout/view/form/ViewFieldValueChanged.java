/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Shared {@link ControlCommand} that handles value-change events from lean view input controls.
 *
 * <p>
 * Each control that supports value changes registers this singleton command and implements
 * {@link ValueChangeHandler} to receive the raw value from the client.
 * </p>
 */
public class ViewFieldValueChanged extends ControlCommand {

	/** Command identifier sent by React components when a field value changes. */
	private static final String COMMAND_ID = "valueChanged";

	/** State key under which the new value is sent. */
	private static final String VALUE = "value";

	/** Singleton instance. */
	public static final ViewFieldValueChanged INSTANCE = new ViewFieldValueChanged();

	private ViewFieldValueChanged() {
		super(COMMAND_ID);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.FIELD_VALUE_CHANGED;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control,
			Map<String, Object> arguments) {
		((ValueChangeHandler) control).handleValueChanged(arguments.get(VALUE));
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Callback for value changes from view field controls.
	 */
	public interface ValueCallback {

		/**
		 * Called when the value of a field control changes.
		 *
		 * @param newValue
		 *        The new value.
		 */
		void valueChanged(Object newValue);
	}

	/**
	 * Interface implemented by controls that handle value-change commands.
	 */
	public interface ValueChangeHandler {

		/**
		 * Processes a raw value received from the client.
		 *
		 * @param rawValue
		 *        The value as received from the JSON arguments.
		 */
		void handleValueChanged(Object rawValue);
	}
}
