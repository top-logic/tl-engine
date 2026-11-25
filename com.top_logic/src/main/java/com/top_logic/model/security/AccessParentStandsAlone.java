/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.util.ResKey;

/**
 * Constraint rejecting an access parent on a type that is marked to need no access check or to be
 * internal.
 * <p>
 * The first checked property holds the {@link AccessParentConfig access parent setting}; the others
 * hold the marks of the same entry. A type delegating its access decision to another object
 * contradicts a mark deciding the access of all its objects at once, so a
 * {@link AccessParentConfig#delegates(AccessParentConfig) delegating} setting is reported as the
 * problem whenever one of the marks is set.
 * </p>
 * <p>
 * The grants of the type are no contradiction: they decide the operations not delegated, and the
 * delegated ones as well, when the setting names the object itself as candidate. Grants the access
 * parent ignores are reported by {@link IgnoredGrants}.
 * </p>
 * 
 * @see SecurityConfigurationService.TLClassAccessRights#getAccessParent()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AccessParentStandsAlone extends GenericPropertyConstraint {

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length == 0) {
			return;
		}
		PropertyModel<?> self = models[0];
		if (!(self.getValue() instanceof AccessParentConfig config) || !AccessParentConfig.delegates(config)) {
			return;
		}
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> other = models[n];
			if (Boolean.TRUE.equals(other.getValue())) {
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
	static ResKey label(PropertyModel<?> model) {
		return ResKey.fallback(model.getProperty().labelKey(null), model.getLabel());
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
