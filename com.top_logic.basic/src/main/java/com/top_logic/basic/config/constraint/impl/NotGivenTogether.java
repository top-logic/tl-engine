/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;

/**
 * Constraint forbidding a value in the annotated property, if any of the referenced properties has
 * a value.
 *
 * <p>
 * Used for alternative settings, of which at most one may be given.
 * </p>
 *
 * @see ValueGivenConstraint#hasValue(PropertyModel)
 * @see MandatoryIfNoneGiven
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NotGivenTogether extends ValueGivenConstraint {

	/**
	 * Singleton {@link NotGivenTogether} instance.
	 */
	public static final NotGivenTogether INSTANCE = new NotGivenTogether();

	private NotGivenTogether() {
		// Singleton constructor.
	}

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length == 0) {
			return;
		}
		PropertyModel<?> self = models[0];
		if (!hasValue(self)) {
			return;
		}
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> model = models[n];
			if (hasValue(model)) {
				self.setProblemDescription(I18NConstants.MUST_NOT_BE_SET_TOGETHER_WITH__OTHER.fill(model.getLabel()));
				return;
			}
		}
	}

}
