/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;

/**
 * Constraint requiring a value in the annotated property, if any of the referenced properties has
 * a value.
 *
 * <p>
 * Properties that are only given together, e.g. a user name and its password, are annotated
 * symmetrically, each with a {@link Constraint} referencing the others.
 * </p>
 *
 * @see ValueGivenConstraint#hasValue(PropertyModel)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MandatoryIfGiven extends ValueGivenConstraint {

	/**
	 * Singleton {@link MandatoryIfGiven} instance.
	 */
	public static final MandatoryIfGiven INSTANCE = new MandatoryIfGiven();

	private MandatoryIfGiven() {
		// Singleton constructor.
	}

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length == 0) {
			return;
		}
		PropertyModel<?> self = models[0];
		if (hasValue(self)) {
			return;
		}
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> model = models[n];
			if (hasValue(model)) {
				self.setProblemDescription(I18NConstants.MUST_BE_SET_IF_OTHER_IS_SET__OTHER.fill(model.getLabel()));
				return;
			}
		}
	}

}
