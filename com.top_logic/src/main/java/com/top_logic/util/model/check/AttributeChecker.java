/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model.check;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.util.ConstraintCheck;

/**
 * {@link InstanceCheck} validating a single {@link ConstraintCheck} on a given object.
 * 
 * @see TLConstraints
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class AttributeChecker extends AbstractInstanceCheck {
	private final ConstraintCheck _check;

	/** 
	 * Creates a {@link AttributeChecker}.
	 */
	public AttributeChecker(ConstraintCheck check, TLStructuredTypePart attribute) {
		super(attribute);
		_check = check;
	}

	@Override
	protected void internalCheck(Sink<ResKey> problems, TLObject object) {
		ResKey problem = _check.check(object, getAttribute());
		if (problem != null) {
			problems.add(withLocation(object, problem));
		}
	}

}