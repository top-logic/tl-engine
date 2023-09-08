/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;

import java.util.Arrays;

/**
 * Base class for {@link Condition} combinations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class CombinedCondition extends Condition {

	private Condition[] _conditions;

	CombinedCondition(Condition... conditions) {
		_conditions = conditions;
	}

	/**
	 * The combined {@link Condition}s.
	 */
	public final Condition[] conditions() {
		return _conditions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(_conditions);
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
		CombinedCondition other = (CombinedCondition) obj;
		if (!Arrays.equals(_conditions, other._conditions))
			return false;
		return true;
	}

}
