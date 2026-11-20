/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collection;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.util.ResKey;

/**
 * Constraint rejecting an access parent on a type that has a definition of its own.
 * <p>
 * The first checked property holds the {@link AccessParentConfig access parent setting}; the others
 * hold the grants and the marks of the same entry. A type delegating its access decision to another
 * object has no grants and no marks, so a {@link AccessParentConfig#delegates(AccessParentConfig)
 * delegating} setting is reported as the problem whenever one of the others is set.
 * </p>
 * <p>
 * A setting {@link AccessParentConfig#isRestricted(AccessParentConfig) restricted} to some
 * operations leaves the other operations to the grants of the type, so only the marks are reported
 * beside it. Whether a grant names a delegated operation is checked by the
 * {@link SecurityConfigurationService}, which resolves the operations.
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
		if (!(self.getValue() instanceof AccessParentConfig config) || !AccessParentConfig.delegates(config)) {
			return;
		}
		boolean restricted = AccessParentConfig.isRestricted(config);
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> other = models[n];
			if (restricted && other.getValue() instanceof Collection<?>) {
				// The grants of the operations not delegated.
				continue;
			}
			if (isSet(other.getValue())) {
				self.setProblemDescription(
					I18NConstants.ACCESS_PARENT_EXCLUDES_OWN_DEFINITION__PROPERTY.fill(label(other)));
			}
		}
	}

	/**
	 * The label the user knows the given property by, its technical name if it has none.
	 *
	 * <p>
	 * The label of the model is the name of the property, which the user editing the access rights
	 * never sees.
	 * </p>
	 */
	private static ResKey label(PropertyModel<?> model) {
		return ResKey.fallback(model.getProperty().labelKey(null), model.getLabel());
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
