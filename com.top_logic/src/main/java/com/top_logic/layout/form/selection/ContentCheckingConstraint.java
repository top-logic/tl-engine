/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.List;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.SimpleProxyConstraint;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.list.model.ListModelUtilities;

/**
 * Constraint checking that the list represented by the given {@link ListField} fulfills the given
 * constraint.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class ContentCheckingConstraint extends SimpleProxyConstraint {

	private final ListField _field;

	ContentCheckingConstraint(Constraint constraint, ListField field) {
		super(constraint);
		_field = field;
	}

	@Override
	public boolean check(Object value) throws CheckException {
		return super.check(getValueToCheck());
	}

	private List<Object> getValueToCheck() {
		return ListModelUtilities.asList(_field.getListModel());
	}
}
