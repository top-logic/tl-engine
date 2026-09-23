/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.impl;

import com.top_logic.basic.config.constraint.algorithm.GenericPropertyConstraint;
import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;

/**
 * {@link GenericPropertyConstraint} ensuring that the annotated boolean property is not
 * <code>true</code> while any of the referenced boolean properties is <code>true</code>, too.
 *
 * <p>
 * Flags that exclude each other are annotated symmetrically, each with a {@link Constraint}
 * referencing the others, so that the problem is reported at whichever of them the user switches on
 * last.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NotBothTrue extends GenericPropertyConstraint {

	/**
	 * Singleton {@link NotBothTrue} instance.
	 */
	public static final NotBothTrue INSTANCE = new NotBothTrue();

	private NotBothTrue() {
		// Singleton constructor.
	}

	@Override
	public void check(PropertyModel<?>... models) {
		if (models.length == 0) {
			return;
		}
		PropertyModel<?> self = models[0];
		if (!isTrue(self)) {
			return;
		}
		for (int n = 1, cnt = models.length; n < cnt; n++) {
			PropertyModel<?> model = models[n];
			if (isTrue(model)) {
				self.setProblemDescription(I18NConstants.MUST_NOT_BE_SET_TOGETHER_WITH__OTHER.fill(model.getLabel()));
			}
		}
	}

	private static boolean isTrue(PropertyModel<?> model) {
		return Boolean.TRUE.equals(model.getValue());
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
