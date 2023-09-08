/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueDependency;

/**
 * {@link ValueDependency} ensuring that the annotated property is not set, if any of the referenced
 * properties is set.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OnlySetIfUnset extends GenericPropertyConstraint {

	/**
	 * Singleton {@link OnlySetIfUnset} instance.
	 */
	public static final OnlySetIfUnset INSTANCE = new OnlySetIfUnset();

	private OnlySetIfUnset() {
		// Singleton constructor.
	}

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length == 0) {
			return;
		}

		PropertyModel<?> self = models[0];
		if (!self.isValueSet()) {
			return;
		}

		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> model = models[n];
			if (model.isValueSet()) {
				self.setProblemDescription(
					I18NConstants.MUST_ONLY_BE_SET_IF_OTHER_IS_UNSET__OTHER.fill(model.getLabel()));
			}
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
