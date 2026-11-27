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
import com.top_logic.model.annotate.security.AccessRevoke;
import com.top_logic.model.annotate.security.AccessRule;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * Constraint reporting the grants of a type that its access parent ignores, meant to be checked as
 * a warning.
 *
 * <p>
 * The first checked property holds the {@link AccessParentConfig access parent setting}, the
 * second the grants of the same entry. A grant of an operation the setting
 * {@link AccessParentConfig#delegatesOperation(AccessParentConfig, String) delegates} - one of the
 * {@link OperationRestriction#getOperations() operations listed}, Discover, which is delegated
 * together with any of them unless it is {@link OperationRestriction#getDiscover() implied} by
 * them, or every operation of a setting not restricted - to some roles takes
 * no effect,
 * unless the setting {@link AccessParentConfig#includesSelf(AccessParentConfig) names the object
 * itself} as a candidate. The operations are compared by their names, as written in the
 * configuration.
 * </p>
 *
 * <p>
 * Such a grant is no error: the grants may be declared by a module, and the access parent by an
 * application, in the same entry merged from both. The application either names the object itself
 * as candidate, or revokes the grants.
 * </p>
 *
 * @see SecurityConfigurationService.TLClassAccessRights#getAccessParent()
 */
public class IgnoredGrants extends GenericPropertyConstraint {

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length < 2) {
			return;
		}
		PropertyModel<?> self = models[0];
		if (!(self.getValue() instanceof AccessParentConfig config) || !AccessParentConfig.delegates(config)
			|| AccessParentConfig.includesSelf(config)) {
			return;
		}
		PropertyModel<?> grants = models[1];
		Collection<?> rules = (Collection<?>) grants.getValue();
		if (rules == null || rules.isEmpty()) {
			return;
		}
		Set<String> ignored = new TreeSet<>();
		for (Object rule : rules) {
			if (!(rule instanceof AccessRule grant) || grant instanceof AccessRevoke) {
				// A revocation takes the grants of other definitions away, which is the way to
				// leave the delegated operations to the access parent.
				continue;
			}
			if (grant.getRoles().isEmpty()) {
				// Grants nothing that could be ignored.
				continue;
			}
			CommandGroupReference operation = grant.getOperation();
			if (operation != null && AccessParentConfig.delegatesOperation(config, operation.id())) {
				ignored.add(operation.id());
			}
		}
		if (!ignored.isEmpty()) {
			self.setProblemDescription(I18NConstants.ACCESS_PARENT_IGNORES_GRANTED__PROPERTY_OPERATIONS
				.fill(AccessParentStandsAlone.label(grants), OperationLabels.of(ignored)));
		}
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
