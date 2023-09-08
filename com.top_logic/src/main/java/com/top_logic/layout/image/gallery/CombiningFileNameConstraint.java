/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractConstraint;

/**
 * {@link AbstractConstraint}, that combines several {@link AbstractConstraint}s to a single one.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class CombiningFileNameConstraint extends AbstractConstraint {

	private AbstractConstraint[] _constraints;

	/**
	 * Create a new {@link CombiningFileNameConstraint}.
	 */
	public CombiningFileNameConstraint(AbstractConstraint... constraints) {
		_constraints = constraints;
	}

	@Override
	public boolean check(Object value) throws CheckException {
		boolean success = true;
		for (AbstractConstraint abstractConstraint : _constraints) {
			success &= abstractConstraint.check(value);
		}
		return success;
	}

}
