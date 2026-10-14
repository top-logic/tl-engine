/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueDependency;
import com.top_logic.basic.util.ResKey;

/**
 * {@link ValueDependency} ensuring that the annotated property is set, if any of the referenced
 * properties is set.
 *
 * <p>
 * A referenced property whose value is an empty {@link Collection} counts as unset, so a list
 * property given without entries demands nothing.
 * </p>
 *
 * @see MandatoryIfUnset
 */
public class MandatoryIfSet extends GenericPropertyConstraint {

	/**
	 * Singleton {@link MandatoryIfSet} instance.
	 */
	public static final MandatoryIfSet INSTANCE = new MandatoryIfSet();

	private MandatoryIfSet() {
		// Singleton constructor.
	}

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length < 2) {
			return;
		}

		PropertyModel<?> self = models[0];
		if (self.isValueSet()) {
			return;
		}

		List<ResKey> others = new ArrayList<>(models.length - 1);
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> model = models[n];
			if (isGiven(model)) {
				others.add(model.getLabel());
			}
		}
		if (others.isEmpty()) {
			return;
		}

		self.setProblemDescription(I18NConstants.MUST_BE_SET_IF_OTHERS_ARE_SET__OTHERS.fill(others));
	}

	private static boolean isGiven(PropertyModel<?> model) {
		if (!model.isValueSet()) {
			return false;
		}
		return !(model.getValue() instanceof Collection<?> collection) || !collection.isEmpty();
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
