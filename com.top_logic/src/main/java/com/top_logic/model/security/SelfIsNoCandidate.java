/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import java.util.List;

import com.top_logic.basic.config.constraint.algorithm.PropertyModel;
import com.top_logic.basic.config.constraint.algorithm.ValueConstraint;

/**
 * Constraint rejecting {@link SelfAccessParent} among the
 * {@link AnyOfAccessParent.Config#getCandidates() candidates} of an {@link AnyOfAccessParent}.
 *
 * <p>
 * A type deciding for itself has no access parent, so <code>&lt;self/&gt;</code> is no candidate.
 * The constraint reports the problem on the candidates, before the setting is stored and the access
 * rights are loaded with it.
 * </p>
 */
@SuppressWarnings("rawtypes")
public class SelfIsNoCandidate extends ValueConstraint<List> {

	/**
	 * Singleton {@link SelfIsNoCandidate} instance.
	 */
	public static final SelfIsNoCandidate INSTANCE = new SelfIsNoCandidate();

	private SelfIsNoCandidate() {
		super(List.class);
	}

	@Override
	protected void checkValue(PropertyModel<List> propertyModel) {
		List<?> candidates = propertyModel.getValue();
		if (candidates == null) {
			return;
		}
		for (Object candidate : candidates) {
			if (candidate instanceof SelfAccessParent.Config) {
				propertyModel.setProblemDescription(I18NConstants.ERROR_SELF_IN_ANY_OF);
				return;
			}
		}
	}

}
