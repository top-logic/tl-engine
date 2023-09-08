/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

/**
 * Row object for the compare table in the {@link CompareService}.
 * 
 * <p>
 * Holder object containing the new row object and the compare row object.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareRowObject {

	private final Object _changeValue;

	private final Object _baseValue;

	/**
	 * Creates a new {@link CompareRowObject}.
	 * 
	 * @param baseValue
	 *        see {@link #changeValue()}
	 * @param changeValue
	 *        see {@link #baseValue()}
	 */
	public CompareRowObject(Object baseValue, Object changeValue) {
		_baseValue = baseValue;
		_changeValue = changeValue;
	}

	/**
	 * The row object, that is comparison change value.
	 */
	public Object changeValue() {
		return _changeValue;
	}

	/**
	 * The row object, that is the comparison base.
	 */
	public Object baseValue() {
		return _baseValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_changeValue == null) ? 0 : _changeValue.hashCode());
		result = prime * result + ((_baseValue == null) ? 0 : _baseValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompareRowObject other = (CompareRowObject) obj;
		if (_changeValue == null) {
			if (other._changeValue != null)
				return false;
		} else if (!_changeValue.equals(other._changeValue))
			return false;
		if (_baseValue == null) {
			if (other._baseValue != null)
				return false;
		} else if (!_baseValue.equals(other._baseValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CompareRowObject [_newValue=").append(_changeValue).append(", _oldValue=").append(_baseValue)
			.append("]");
		return builder.toString();
	}

}

