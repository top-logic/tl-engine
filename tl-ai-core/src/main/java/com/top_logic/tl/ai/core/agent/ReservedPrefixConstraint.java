/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} that rejects names starting with the {@code TL_} prefix.
 *
 * <p>
 * The {@code TL_} prefix is reserved for platform-level agent and skill definitions shipped with
 * TopLogic (see {@link TopLogicAgentDefinitions}). Application-defined agents and skills must
 * choose a different prefix.
 * </p>
 */
public class ReservedPrefixConstraint implements ConstraintCheck {

	/** Prefix reserved for TopLogic platform definitions. */
	private static final String RESERVED_PREFIX = "TL_";

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		Object value = object.tValue(attribute);
		if (value instanceof String name && name.startsWith(RESERVED_PREFIX)) {
			return I18NConstants.RESERVED_PREFIX_ERROR__NAME.fill(name);
		}
		return null;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			FormContext formContext) {
		// Only depends on the annotated attribute's own value — no additional dependencies.
	}
}
