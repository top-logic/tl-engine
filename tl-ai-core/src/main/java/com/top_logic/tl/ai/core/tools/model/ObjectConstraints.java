/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.TLRange;
import com.top_logic.model.annotate.TLSize;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.model.check.AttributeChecker;
import com.top_logic.util.model.check.InstanceCheck;
import com.top_logic.util.model.check.NumberRangeCheck;
import com.top_logic.util.model.check.StringSizeCheck;

/**
 * The model's constraints on one object, asked before the turn commits.
 *
 * <p>
 * <b>The engine decides; this only asks earlier.</b> {@link ModelService} is a commit checker: it
 * vetoes a commit in which an object violates a mandatory attribute, a {@link TLSize}, a
 * {@link TLRange} or a {@link TLConstraints} error check. That veto stays the authority. But it
 * arrives at the end of the turn, rolls back every change of the turn, and reaches the model as a
 * framework notice after the tool already said "done". Asking the same questions inside the tool lets
 * it refuse its own change, undo it and say which attribute is wrong - while everything else the turn
 * did survives.
 * </p>
 *
 * <p>
 * The checks are assembled exactly as {@code ModelService} assembles them for a type, so the two
 * cannot disagree about which constraints exist. The engine's method for it is private; if it ever
 * becomes public, this class should call it instead.
 * </p>
 */
final class ObjectConstraints {

	private ObjectConstraints() {
		// Static utility.
	}

	/**
	 * Every constraint the given object violates, one sentence each, naming the attribute.
	 *
	 * @return The problems; empty if the commit would accept the object.
	 */
	static List<String> violations(TLObject object) {
		List<String> result = new ArrayList<>();
		TLStructuredType type = object.tType();
		if (type == null) {
			return result;
		}
		Resources english = Resources.getInstance(Locale.ENGLISH);
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part.isDerived()) {
				continue;
			}
			for (InstanceCheck check : checks(part)) {
				List<ResKey> problems = new ArrayList<>();
				check.check(problems::add, object);
				for (ResKey problem : problems) {
					// The engine's sentence names the attribute by its label; the model writes the
					// name, so the name goes in front.
					result.add("'" + part.getName() + "': " + english.getString(problem));
				}
			}
		}
		return result;
	}

	/**
	 * The checks {@code ModelService.createCheck} builds for one attribute: the storage's own (the
	 * mandatory check lives there), the size and range annotations, and the error-level constraint
	 * checks.
	 */
	private static List<InstanceCheck> checks(TLStructuredTypePart part) {
		List<InstanceCheck> checks = new ArrayList<>();
		part.getStorageImplementation().addConstraints(part, checks);
		TLSize size = part.getAnnotation(TLSize.class);
		if (size != null) {
			checks.add(new StringSizeCheck(part, size.getLowerBound(), size.getUpperBound()));
		}
		TLRange range = part.getAnnotation(TLRange.class);
		if (range != null) {
			checks.add(new NumberRangeCheck(part, range.getMinimum(), range.getMaximum()));
		}
		TLConstraints constraints = part.getAnnotation(TLConstraints.class);
		if (constraints != null) {
			for (PolymorphicConfiguration<? extends ConstraintCheck> config : constraints.getConstraints()) {
				ConstraintCheck check =
					SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
				if (check.type() == ConstraintCheck.ConstraintType.ERROR) {
					checks.add(new AttributeChecker(check, part));
				}
			}
		}
		return checks;
	}
}
