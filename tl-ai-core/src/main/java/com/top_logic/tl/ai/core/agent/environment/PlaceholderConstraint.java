/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.List;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} that refuses a system prompt naming a placeholder the harness cannot fill.
 *
 * <p>
 * This is the knowledge-base half of the check; the configuration half runs while the shipped
 * definitions are read (see {@code TopLogicAgentDefinitions.checkPlaceholders}). Both exist because
 * a placeholder nobody fills reaches the model verbatim: it reads as an instruction the harness
 * failed to honour, and the typo behind it is invisible in the answer.
 * </p>
 *
 * @see PromptPlaceholders
 */
public class PlaceholderConstraint implements ConstraintCheck {

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		Object value = object.tValue(attribute);
		if (!(value instanceof String prompt)) {
			return null;
		}
		List<String> unknown = PromptPlaceholders.unknownIn(prompt);
		if (unknown.isEmpty()) {
			return null;
		}
		String names = String.join(", ", unknown.stream().map(name -> "{{" + name + "}}").toList());
		return I18NConstants.UNKNOWN_PLACEHOLDER__NAMES_KNOWN.fill(names,
			PromptPlaceholders.knownNames());
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// Only depends on the annotated attribute's own value.
	}
}
