/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.annotate.AccessRightsConfig;
import com.top_logic.model.annotate.security.AccessRule;
import com.top_logic.tool.boundsec.CommandGroupReference;

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
 * operations leaves the other operations to the grants of the type. Beside it, a mark is reported,
 * and a grant of an operation the setting delegates: one of the
 * {@link OperationRestriction#getOperations() operations listed}, or Discover, which is delegated
 * together with any of them. The operations are compared by their names, as written in the
 * configuration.
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
		Set<String> delegated = AccessParentConfig.delegatedOperationNames(config);
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> other = models[n];
			if (isGrants(other)) {
				checkGrants(self, other, delegated);
			} else if (Boolean.TRUE.equals(other.getValue())) {
				self.setProblemDescription(
					I18NConstants.ACCESS_PARENT_EXCLUDES_OWN_DEFINITION__PROPERTY.fill(label(other)));
			}
		}
	}

	/**
	 * Reports the grants of the given property that name a delegated operation.
	 *
	 * @param delegated
	 *        The names of the delegated operations, <code>null</code> for all.
	 */
	private static void checkGrants(PropertyModel<?> self, PropertyModel<?> grants, Set<String> delegated) {
		Collection<?> rules = (Collection<?>) grants.getValue();
		if (rules == null || rules.isEmpty()) {
			return;
		}
		if (delegated == null) {
			self.setProblemDescription(
				I18NConstants.ACCESS_PARENT_EXCLUDES_OWN_DEFINITION__PROPERTY.fill(label(grants)));
			return;
		}
		Set<String> granted = new TreeSet<>();
		for (Object rule : rules) {
			CommandGroupReference operation = ((AccessRule) rule).getOperation();
			if (operation != null && delegated.contains(operation.id())) {
				granted.add(operation.id());
			}
		}
		if (!granted.isEmpty()) {
			self.setProblemDescription(I18NConstants.ACCESS_PARENT_DECIDES_GRANTED__PROPERTY_OPERATIONS
				.fill(label(grants), String.join(", ", granted)));
		}
	}

	/**
	 * Whether the given property holds the grants of the entry, all other checked properties being
	 * marks.
	 */
	private static boolean isGrants(PropertyModel<?> model) {
		return AccessRightsConfig.GRANTS.equals(model.getProperty().getPropertyName());
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

	@Override
	public boolean isChecked(int index) {
		return index == 0;
	}

	@Override
	public Class<?>[] signature() {
		return null;
	}

}
