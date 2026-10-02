/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;

/**
 * Base class for constraints relating the annotated property to the presence of values in the
 * referenced properties.
 *
 * <p>
 * A property has a value, if its value is neither <code>null</code>, nor an empty string, nor an
 * empty collection or map. In contrast to the explicit assignment of a property, this also treats
 * an empty value as missing, e.g. a value taken from an unset environment variable. Only the
 * annotated property is reported as faulty.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ValueGivenConstraint extends GenericPropertyConstraint {

	/**
	 * Whether the given property has a value.
	 */
	public static boolean hasValue(PropertyModel<?> model) {
		Object value = model.getValue();
		if (value == null) {
			return false;
		}
		if (value instanceof CharSequence text) {
			return text.length() > 0;
		}
		if (value instanceof Collection<?> collection) {
			return !collection.isEmpty();
		}
		if (value instanceof Map<?, ?> map) {
			return !map.isEmpty();
		}
		return true;
	}

	@Override
	public boolean isChecked(int index) {
		return index == 0;
	}

	@Override
	public Class<?>[] signature() {
		return null;
	}

}
