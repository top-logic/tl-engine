/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener;

import com.top_logic.layout.form.FormField;

/**
 * The {@link PresetValueListenerDependency} copies the value of the source field into the
 * destination field, if the source field gets changed and the target field wasn't changed by the
 * user.
 * 
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class PresetValueListenerDependency extends CopyValueListenerDependency {

	/**
	 * Creates a new {@link PresetValueListenerDependency}.
	 * 
	 * @param aSourceField
	 *        the field to copy changes from
	 * @param aDestinationField
	 *        the field to keep in sync with the source field as long it wasn't changed by the user.
	 */
	public PresetValueListenerDependency(FormField aSourceField, FormField aDestinationField) {
		super(aSourceField, aDestinationField);
	}

	@Override
	protected void touchAllImplementation() {
		Object result = aggregateOverFields();
		FormField resField = getResultField();
		if (resField != null && !resField.isChanged()) {
			resField.initializeField(result);
		}
	}

}
