/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;

/**
 * The modes of a {@link FormField} that a script can check.
 * 
 * <ul>
 * <li>{@link #ACTIVE}</li>
 * <li>{@link #DISABLED}</li>
 * <li>{@link #IMMUTABLE}</li>
 * <li>{@link #INVISIBLE}</li>
 * </ul>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public enum FieldMode {

	/**
	 * The field displays an input element that is active.
	 * 
	 * @implNote A {@link FormField} is {@link #ACTIVE}, if and only if
	 *           {@link FormField#isActive()} is true.
	 */
	ACTIVE,

	/**
	 * The field displays an input element that is disabled.
	 * 
	 * @implNote A {@link FormField} is {@link #DISABLED}, if and only if
	 *           {@link FormField#isDisabled()} is true.
	 */
	DISABLED,

	/**
	 * The field displays only its value, it cannot be edited.
	 * 
	 * @implNote A {@link FormField} is {@link #IMMUTABLE}, if and only if
	 *           {@link FormField#isImmutable()} is true.
	 */
	IMMUTABLE,

	/**
	 * The field displays a placeholder instead of its value, because the current user is not
	 * allowed to see the value.
	 * 
	 * @implNote A {@link FormField} is {@link #BLOCKED}, if and only if
	 *           {@link FormField#isBlocked()} is true.
	 */
	BLOCKED,

	/**
	 * The field is not displayed at all.
	 * 
	 * @implNote A {@link FormField} is {@link #INVISIBLE}, if and only if
	 *           {@link FormField#isVisible()} is false.
	 */
	INVISIBLE;

	/** The {@link FieldMode} the given field is currently in. */
	public static FieldMode getMode(FormMember field) {
		if (field.isActive()) {
			return FieldMode.ACTIVE;
		}
		if (!field.isVisible()) {
			return FieldMode.INVISIBLE;
		}
		if (field instanceof FormField && ((FormField) field).isBlocked()) {
			return FieldMode.BLOCKED;
		}
		if (field.isImmutable()) {
			return FieldMode.IMMUTABLE;
		}
		if (field.isDisabled()) {
			return FieldMode.DISABLED;
		}
		throw new UnreachableAssertion("This field is not active, not immutable, not disabled but visible.");
	}

}
