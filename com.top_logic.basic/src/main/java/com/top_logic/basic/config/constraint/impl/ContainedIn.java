/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;

/**
 * Constraint requiring the value of the annotated property to be a key of the referenced map
 * property, or an element of the referenced collection property.
 *
 * <p>
 * Used for a property naming one of the entries configured in another property, e.g. the default
 * among several named configurations. An empty value (<code>null</code> or an empty string) is not
 * checked. Only the annotated property is reported as faulty.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContainedIn extends GenericPropertyConstraint {

	/**
	 * Singleton {@link ContainedIn} instance.
	 */
	public static final ContainedIn INSTANCE = new ContainedIn();

	private ContainedIn() {
		// Singleton constructor.
	}

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length < 2) {
			return;
		}
		PropertyModel<?> self = models[0];
		if (!ValueGivenConstraint.hasValue(self)) {
			return;
		}
		Object value = self.getValue();
		PropertyModel<?> other = models[1];
		Collection<?> entries = entries(other.getValue());
		if (entries.contains(value)) {
			return;
		}
		self.setProblemDescription(
			I18NConstants.NOT_CONTAINED_IN__VALUE__OTHER__ENTRIES.fill(value, other.getLabel(), entries));
	}

	private static Collection<?> entries(Object container) {
		if (container instanceof Map<?, ?> map) {
			return map.keySet();
		}
		if (container instanceof Collection<?> collection) {
			return collection;
		}
		return List.of();
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
