/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.Arrays;

/**
 * Helper class for {@link IdMapperImpl}, that just holds a sequence of values and a type name.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ValueKey {

	private Object[] _values;

	private String _type;

	public ValueKey(String type, int size) {
		this(type, new Object[size]);
	}

	public ValueKey(String type, Object[] values) {
		_type = type;
		_values = values;
	}

	public Object get(int n) {
		return _values[n];
	}

	public void set(int n, Object value) {
		_values[n] = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(_values);
		result = prime * result + _type.hashCode();
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
		ValueKey other = (ValueKey) obj;
		if (!_type.equals(other._type))
			return false;
		if (!Arrays.equals(_values, other._values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return _type + ": " + Arrays.deepToString(_values);
	}

}
