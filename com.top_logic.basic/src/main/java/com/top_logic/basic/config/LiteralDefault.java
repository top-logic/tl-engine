/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.tools.NameBuilder;

/**
 * {@link DefaultSpec} that produces a literal value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class LiteralDefault extends DefaultSpec {

	private final Object _value;

	public LiteralDefault(Object value) {
		_value = value;
	}

	@Override
	public Object getDefaultValue(PropertyDescriptor property) {
		return _value;
	}

	@Override
	public boolean isShared(PropertyDescriptor property) {
		return true;
	}

	public Object getValue() {
		return _value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_value == null) ? 0 : _value.hashCode());
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
		LiteralDefault other = (LiteralDefault) obj;
		if (_value == null) {
			if (other._value != null)
				return false;
		} else if (!_value.equals(other._value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new NameBuilder(this).addUnnamed(_value.toString()).buildName();
	}
}
