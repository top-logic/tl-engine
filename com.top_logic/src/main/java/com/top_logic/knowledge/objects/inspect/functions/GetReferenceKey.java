/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.functions;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.condition.ItemFunction;

/**
 * {@link ItemFunction} accessing a certain reference retrieving the stored {@link ObjectKey}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GetReferenceKey extends ItemFunction {

	private final MOReference _reference;

	/**
	 * Creates a {@link GetReferenceKey}.
	 *
	 * @param reference See {@link #reference()}.
	 */
	public GetReferenceKey(MOReference reference) {
		_reference = reference;
	}
	
	/**
	 * The {@link MOReference} to access.
	 */
	public final MOReference reference() {
		return _reference;
	}

	@Override
	public Object apply(KnowledgeItem item) {
		return item.getReferencedKey(_reference);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_reference == null) ? 0 : _reference.hashCode());
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
		GetReferenceKey other = (GetReferenceKey) obj;
		if (_reference == null) {
			if (other._reference != null)
				return false;
		} else if (!_reference.equals(other._reference))
			return false;
		return true;
	}

}
