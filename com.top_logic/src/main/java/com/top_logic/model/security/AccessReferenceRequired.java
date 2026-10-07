/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;

/**
 * Constraint requiring an access reference for an access parent that is the
 * {@link AccessParentKind#TARGET target of a reference}.
 * <p>
 * The first checked property holds the access reference, the second the kind of access parent of
 * the same entry. The {@link AccessParentKind#CONTAINER container} needs no reference, it then
 * stands for whichever composition holds the object.
 * </p>
 * 
 * @see SecurityConfigurationService.TLClassAccessRights#getAccessReference()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AccessReferenceRequired extends GenericPropertyConstraint {

	/**
	 * Singleton {@link AccessReferenceRequired} instance.
	 */
	public static final AccessReferenceRequired INSTANCE = new AccessReferenceRequired();

	private AccessReferenceRequired() {
		// Singleton constructor.
	}

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length < 2) {
			return;
		}
		PropertyModel<?> self = models[0];
		if (self.getValue() == null && models[1].getValue() == AccessParentKind.TARGET) {
			self.setProblemDescription(I18NConstants.ACCESS_REFERENCE_REQUIRED);
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
