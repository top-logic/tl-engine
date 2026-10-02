/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.util.ResKey;

/**
 * Constraint requiring a value in the annotated property, if none of the referenced properties
 * has a value.
 *
 * <p>
 * Used for alternative settings, of which at least one must be given.
 * </p>
 *
 * @see ValueGivenConstraint#hasValue(PropertyModel)
 * @see NotGivenTogether
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MandatoryIfNoneGiven extends ValueGivenConstraint {

	/**
	 * Singleton {@link MandatoryIfNoneGiven} instance.
	 */
	public static final MandatoryIfNoneGiven INSTANCE = new MandatoryIfNoneGiven();

	private MandatoryIfNoneGiven() {
		// Singleton constructor.
	}

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length < 2) {
			return;
		}
		PropertyModel<?> self = models[0];
		if (hasValue(self)) {
			return;
		}
		List<ResKey> alternatives = new ArrayList<>(models.length - 1);
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> model = models[n];
			if (hasValue(model)) {
				return;
			}
			alternatives.add(model.getLabel());
		}
		self.setProblemDescription(I18NConstants.MUST_BE_SET_IF_OTHERS_ARE_UNSET__OTHERS.fill(alternatives));
	}

}
