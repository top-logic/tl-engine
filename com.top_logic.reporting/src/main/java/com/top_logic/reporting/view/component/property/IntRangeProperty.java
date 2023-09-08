/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.property;

import com.top_logic.layout.form.constraints.IntRangeConstraint;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.tool.boundsec.BoundComponent;

/**
 * {@link PrimitiveFilterProperty} for ranged intergers.
 *
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class IntRangeProperty extends PrimitiveFilterProperty {

	private final int startRange;

	private final int endRange;

	/**
	 * Creates a new {@link IntRangeProperty}.
	 */
	public IntRangeProperty(String name, Integer initialValue, int startRange, int endRange, BoundComponent aComponent) {
		super(name, initialValue, aComponent);
		this.startRange = startRange;
		this.endRange = endRange;
	}

	@Override
	protected AbstractFormField createNewFormMember() {
		return FormFactory.newIntField(getName(), getInitialValue(), true, false, new IntRangeConstraint(startRange, endRange));
	}

}
