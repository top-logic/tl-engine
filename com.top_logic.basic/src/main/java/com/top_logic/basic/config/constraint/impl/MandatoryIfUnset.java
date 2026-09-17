/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueDependency;
import com.top_logic.basic.util.ResKey;

/**
 * {@link ValueDependency} ensuring that the annotated property is set, if none of the referenced
 * properties is set.
 *
 * <p>
 * Together with {@link OnlySetIfUnset} on each of the properties involved, this expresses that
 * exactly one of a group of alternatives must be given.
 * </p>
 */
public class MandatoryIfUnset extends GenericPropertyConstraint {

	/**
	 * Singleton {@link MandatoryIfUnset} instance.
	 */
	public static final MandatoryIfUnset INSTANCE = new MandatoryIfUnset();

	private MandatoryIfUnset() {
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

		List<ResKey> alternatives = new ArrayList<>(models.length - 1);
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> model = models[n];
			if (model.isValueSet()) {
				return;
			}
			alternatives.add(model.getLabel());
		}

		self.setProblemDescription(I18NConstants.MUST_BE_SET_IF_OTHERS_ARE_UNSET__OTHERS.fill(alternatives));
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
