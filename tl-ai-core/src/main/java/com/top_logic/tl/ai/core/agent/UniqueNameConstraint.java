/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.util.Pointer;

/**
 * {@link ConstraintCheck} that ensures the {@code name} attribute is unique among all instances of
 * the same type.
 *
 * <p>
 * The comparison is case-insensitive to match the persistence-level backstop: the {@code name}
 * columns of {@code AIAgent} / {@code AISkill} are {@code String} columns, which the database
 * renders case-insensitively (e.g. {@code VARCHAR_IGNORECASE} on H2). Aligning both layers ensures
 * the form gives fast feedback for exactly those duplicates the unique DB index would reject.
 * </p>
 */
public class UniqueNameConstraint implements ConstraintCheck {

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		Object value = object.tValue(attribute);
		if (!(value instanceof String name) || name.isEmpty()) {
			return null;
		}

		TLClass type = (TLClass) object.tType();
		for (TLObject other : MetaElementUtil.getAllInstancesOf(type, TLObject.class)) {
			if (other.equals(object) || !other.tValid()) {
				continue;
			}
			if (other.tValue(attribute) instanceof String otherName && name.equalsIgnoreCase(otherName)) {
				return I18NConstants.DUPLICATE_NAME_ERROR__NAME.fill(name);
			}
		}
		return null;
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			FormContext formContext) {
		// Only depends on the annotated attribute's own value — no additional dependencies.
	}
}
