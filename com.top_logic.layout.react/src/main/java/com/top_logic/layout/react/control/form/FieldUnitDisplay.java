/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.layout.react.field.FieldSpec;

/**
 * A control writing the {@link FieldSpec#getUnit() unit of its field} after the value.
 *
 * <p>
 * The unit of a field may depend on the object whose value the field shows. A field that keeps its
 * control when it switches to another object tells the control the unit for that object through
 * {@link #setFieldUnit(String)}.
 * </p>
 *
 * <p>
 * A control writing a unit of its own configuration instead keeps that unit and ignores the unit of
 * its field.
 * </p>
 */
public interface FieldUnitDisplay {

	/**
	 * Sets the unit of the field, written after the value unless the control writes a unit of its
	 * own.
	 *
	 * @param unit
	 *        The text of the unit; {@code null} or an empty text writes no unit.
	 */
	void setFieldUnit(String unit);

}
