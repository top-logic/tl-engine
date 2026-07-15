/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.validation;

import java.util.Collections;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.annotate.util.ConstraintCheck.ConstraintType;
import com.top_logic.model.util.Pointer;

/**
 * Internal bookkeeping for a single constraint instance within
 * {@link FormValidationModel}.
 */
class ConstraintEntry {

	private final ConstraintCheck _check;
	private final TLObject _object;
	private final TLStructuredTypePart _attribute;
	private final ConstraintType _type;
	private Set<Pointer> _dependencies = Collections.emptySet();

	ConstraintEntry(ConstraintCheck check, TLObject object, TLStructuredTypePart attribute) {
		_check = check;
		_object = object;
		_attribute = attribute;
		_type = check.type();
	}

	ConstraintCheck getCheck() {
		return _check;
	}

	TLObject getObject() {
		return _object;
	}

	TLStructuredTypePart getAttribute() {
		return _attribute;
	}

	ConstraintType getType() {
		return _type;
	}

	Set<Pointer> getDependencies() {
		return _dependencies;
	}

	void setDependencies(Set<Pointer> dependencies) {
		_dependencies = dependencies;
	}
}
