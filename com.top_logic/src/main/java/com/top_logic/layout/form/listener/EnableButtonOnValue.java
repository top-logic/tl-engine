/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener;

import java.util.Collection;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;

/**
 * {@link ValueListener} that enables a given button if the observed field gets a non empty value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class EnableButtonOnValue implements ValueListener {

	/**
	 * The button whose executablity is controlled by this listener.
	 */
	private final CommandModel button;

	/**
	 * Creates a {@link EnableButtonOnValue} listener.
	 * 
	 * @param button
	 *        The button whose executablity is adjusted.
	 * 
	 * @see #enableButtonOnNonEmptyValue(FormField, CommandModel)
	 */
	protected EnableButtonOnValue(CommandModel button) {
		this.button = button;
	}

	/**
	 * Installs this listener on the given field and establishes the executability of the controlled
	 * button based on the current value of the given field.
	 * 
	 * @param field
	 *        The {@link FormField} to install this listener on.
	 * @return This listener instance.
	 */
	public EnableButtonOnValue install(FormField field) {
		updateExecutability(field.getValue());
		field.addValueListener(this);
		return this;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		updateExecutability(newValue);
	}

	private void updateExecutability(Object newValue) {
		boolean valueEmpty = isEmpty(newValue);
		if (valueEmpty) {
			button.setNotExecutable(I18NConstants.NOT_EXECUTABLE_VALUE_EMPTY);
		} else {
			button.setExecutable();
		}
	}

	private boolean isEmpty(Object newValue) {
		return isNull(newValue) || isEmptyCollection(newValue);
	}

	private static boolean isNull(Object newValue) {
		return newValue == null;
	}

	private static boolean isEmptyCollection(Object newValue) {
		if (newValue instanceof Collection<?>) {
			return ((Collection<?>) newValue).isEmpty();
		}
		return false;
	}

	/**
	 * Installs an {@link EnableButtonOnValue} listener on the given field controlling the given
	 * button.
	 * 
	 * @param field
	 *        The observed field.
	 * @param button
	 *        The controlled button.
	 * @return The new listener instance.
	 */
	public static EnableButtonOnValue enableButtonOnNonEmptyValue(FormField field, CommandModel button) {
		return new EnableButtonOnValue(button).install(field);
	}
}