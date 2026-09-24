/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collection;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;

/**
 * Constraint rejecting an access parent on a type that has a definition of its own.
 * <p>
 * The first checked property holds the access parent; the others hold the grants and the marks of
 * the same entry. A type delegating its access decision to another object has no grants and no
 * marks, so the access parent is reported as the problem whenever one of the others is set.
 * </p>
 * 
 * @see SecurityConfigurationService.TLClassAccessRights#getAccessParent()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AccessParentStandsAlone extends GenericPropertyConstraint {

	/**
	 * Singleton {@link AccessParentStandsAlone} instance.
	 */
	public static final AccessParentStandsAlone INSTANCE = new AccessParentStandsAlone();

	private AccessParentStandsAlone() {
		// Singleton constructor.
	}

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length == 0) {
			return;
		}
		PropertyModel<?> self = models[0];
		if (self.getValue() == null) {
			return;
		}
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> other = models[n];
			if (isSet(other.getValue())) {
				self.setProblemDescription(
					I18NConstants.ACCESS_PARENT_EXCLUDES_OWN_DEFINITION__PROPERTY.fill(other.getLabel()));
			}
		}
	}

	/**
	 * Whether the given value of a grants list or of a mark counts as a definition of the type.
	 */
	private static boolean isSet(Object value) {
		if (value instanceof Collection<?> collection) {
			return !collection.isEmpty();
		}
		return Boolean.TRUE.equals(value);
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
