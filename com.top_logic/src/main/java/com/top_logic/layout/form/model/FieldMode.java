/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;

/**
 * Modes in which a {@link FormField} can be displayed.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum FieldMode implements ExternallyNamed {

	/**
	 * The field displays an input element that is active.
	 */
	ACTIVE("active", true),

	/**
	 * The field displays an input element that is disabled.
	 */
	DISABLED("disabled", true),

	/**
	 * The field displays only the value in a way that is optimized for reading.
	 */
	IMMUTABLE("immutable", true),

	/**
	 * The field is a group, prevent changing the group layout, but do not prevent modifying
	 * contents.
	 * 
	 * <p>
	 * This option is only useful in combination of a {@link DynamicMode} annotation of a
	 * declarative form (as dynamic equivalent to
	 * {@link com.top_logic.basic.config.annotation.ReadOnly.ReadOnlyMode#LOCAL}). This option must
	 * not be used to directly manipulate a field's mode through
	 * {@link FormMember#setMode(FieldMode)}.
	 * </p>
	 */
	LOCALLY_IMMUTABLE("locally-immutable", true),

	/**
	 * The field displays neither input element nor the value, but a placeholder for the value that
	 * gives visual feedback that the current user is not allowed to see the value.
	 */
	BLOCKED("blocked", false),

	/**
	 * The field does not display anything, but hides itself from the UI.
	 */
	INVISIBLE("invisible", false);

	private final String _externalName;

	private final boolean _valueVisible;

	private FieldMode(String externalName, boolean valueVisible) {
		_externalName = externalName;
		_valueVisible = valueVisible;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Whether the value of a field can be seen by the user in this mode.
	 */
	public boolean isValueVisible() {
		return _valueVisible;
	}

}
