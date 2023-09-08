/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.functions;

import com.top_logic.dob.MOAttribute;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.condition.ItemFunction;

/**
 * {@link ItemFunction} accessing the value of a certain attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GetAttribute extends ItemFunction {

	private final MOAttribute _attribute;

	/**
	 * Creates a {@link GetAttribute}.
	 * 
	 * @param attribute
	 *        See {@link #attribute()}.
	 */
	public GetAttribute(MOAttribute attribute) {
		_attribute = attribute;
	}

	/**
	 * The {@link MOAttribute} being accessed.
	 */
	public final MOAttribute attribute() {
		return _attribute;
	}

	@Override
	public Object apply(KnowledgeItem item) {
		return item.getValue(_attribute);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_attribute == null) ? 0 : _attribute.hashCode());
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
		GetAttribute other = (GetAttribute) obj;
		if (_attribute == null) {
			if (other._attribute != null)
				return false;
		} else if (!_attribute.equals(other._attribute))
			return false;
		return true;
	}

}