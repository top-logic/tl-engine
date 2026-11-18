/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.tools.model;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.TLRange;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.TLValueCount;
import com.top_logic.ai.core.tools.refusal.ApplicationRefusals;
import com.top_logic.ai.core.tools.refusal.messages.Reason;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.model.check.InstanceCheck;

/**
 * The model's constraints on one object, asked before the turn commits.
 *
 * <p>
 * <b>The engine decides; this only asks earlier.</b> {@link ModelService} is a commit checker: it
 * vetoes a commit in which an object violates a mandatory attribute, a {@link TLSize}, a
 * {@link TLRange}, a {@link TLValueCount} or a {@link TLConstraints} error check. That veto stays the authority. But it
 * arrives at the end of the turn, rolls back every change of the turn, and reaches the model as a
 * framework notice after the tool already said "done". Asking the same questions inside the tool lets
 * it refuse its own change, undo it and say which attribute is wrong - while everything else the turn
 * did survives.
 * </p>
 *
 * <p>
 * The checks are assembled by {@link ModelService} itself, so the two cannot disagree about which
 * constraints exist.
 * </p>
 */
final class ObjectConstraints {

	private ObjectConstraints() {
		// Static utility.
	}

	/**
	 * Every constraint the given object violates, each with the name of its attribute and the
	 * application's validation message in the language of the run's user.
	 *
	 * @return The problems; empty if the commit would accept the object.
	 */
	static List<Reason> violations(TLObject object) {
		List<Reason> result = new ArrayList<>();
		TLStructuredType type = object.tType();
		if (type == null) {
			return result;
		}
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part.isDerived()) {
				continue;
			}
			for (InstanceCheck check : checks(part)) {
				List<ResKey> problems = new ArrayList<>();
				check.check(problems::add, object);
				for (ResKey problem : problems) {
					// The engine's sentence names the attribute by its label; the model writes the
					// name, so the name is given beside it.
					result.add(ApplicationRefusals.reason(problem).setAttribute(part.getName()));
				}
			}
		}
		return result;
	}

	/**
	 * The checks a commit performs for one attribute, see
	 * {@link ModelService#addChecks(List, TLStructuredTypePart)}.
	 */
	private static List<InstanceCheck> checks(TLStructuredTypePart part) {
		List<InstanceCheck> checks = new ArrayList<>();
		ModelService.addChecks(checks, part);
		return checks;
	}
}
