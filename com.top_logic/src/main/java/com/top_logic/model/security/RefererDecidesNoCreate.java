/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.List;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Constraint rejecting Create among the {@link RefererAccessParent.Config#getOperations()
 * operations} of a {@link RefererAccessParent}.
 *
 * <p>
 * Nothing refers to an object being created, so a referer never decides Create, and loading the
 * access rights rejects a referer listing it. The constraint reports the problem on the operations,
 * before the setting is stored and the access rights are loaded with it.
 * </p>
 */
@SuppressWarnings("rawtypes")
public class RefererDecidesNoCreate extends ValueConstraint<List> {

	/**
	 * Singleton {@link RefererDecidesNoCreate} instance.
	 */
	public static final RefererDecidesNoCreate INSTANCE = new RefererDecidesNoCreate();

	private RefererDecidesNoCreate() {
		super(List.class);
	}

	@Override
	protected void checkValue(PropertyModel<List> propertyModel) {
		List<?> operations = propertyModel.getValue();
		if (operations == null) {
			return;
		}
		for (Object operation : operations) {
			if (operation instanceof CommandGroupReference reference
				&& SimpleBoundCommandGroup.CREATE.getID().equals(reference.id())) {
				propertyModel.setProblemDescription(I18NConstants.ERROR_CREATE_FOR_REFERER);
				return;
			}
		}
	}

}
