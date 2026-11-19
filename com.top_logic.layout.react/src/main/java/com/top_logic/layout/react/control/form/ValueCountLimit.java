/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.field.FieldSpec;

/**
 * The {@link FieldSpec#getMaxCount() maximum number of values} of a field, as a control adding
 * values enforces it.
 *
 * <p>
 * The control publishes the maximum to its client component under {@link #MAX_COUNT}; the client
 * offers no further value once the field holds that many. Values added at once - the files of an
 * upload - are taken all or none: an addition beyond the room left is refused as a whole with the
 * {@link #checkAdd(int, int) problem} naming the maximum, the number of added values and the room
 * left.
 * </p>
 */
public final class ValueCountLimit {

	/**
	 * State key under which a control publishes the {@link FieldSpec#getMaxCount() maximum number of
	 * values} of its field to its client component.
	 *
	 * <p>
	 * A value of <code>0</code> means that the number of values is not limited.
	 * </p>
	 */
	public static final String MAX_COUNT = "maxCount";

	/**
	 * The limit of a field whose number of values is not limited.
	 */
	public static final ValueCountLimit NONE = new ValueCountLimit(0);

	private final int _max;

	private ValueCountLimit(int max) {
		_max = max;
	}

	/**
	 * The limit of the given maximum.
	 *
	 * @param max
	 *        The maximum number of values, <code>0</code> or less for no limit.
	 */
	public static ValueCountLimit of(int max) {
		return max > 0 ? new ValueCountLimit(max) : NONE;
	}

	/**
	 * The maximum number of values, <code>0</code> if the number is not limited.
	 */
	public int getMax() {
		return _max;
	}

	/**
	 * The problem of adding the given number of values to a field holding the given number, or
	 * <code>null</code> if they fit.
	 *
	 * @param count
	 *        The number of values the field holds.
	 * @param added
	 *        The number of values to add at once.
	 * @return The problem naming the maximum, the number of added values and the room left; the
	 *         addition is to be refused as a whole.
	 */
	public ResKey checkAdd(int count, int added) {
		if (_max <= 0 || count + added <= _max) {
			return null;
		}
		return I18NConstants.ERROR_TOO_MANY_VALUES_ADDED__MAX_SELECTED_ROOM.fill(_max, added,
			Math.max(0, _max - count));
	}

}
