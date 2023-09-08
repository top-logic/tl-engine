/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.scripting.action.FormAction;
import com.top_logic.layout.scripting.action.FormInput;

/**
 * {@link AddValueActionOp} adds values to a {@link FormField}.
 * <p>
 * Does not cause duplicate values: Only those values are added, which are not already set.
 * </p>
 * 
 * @deprecated Use {@link FormInput} with
 *             {@link com.top_logic.layout.scripting.action.FormInput.Mode#ADD}.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class AddValueActionOp extends FormInputOp {

	/**
	 * Called by the typed configuration for creating an {@link AddValueActionOp} from the
	 * {@link FormAction}.
	 */
	@CalledByReflection
	public AddValueActionOp(InstantiationContext context, FormAction config) {
		super(context, config);
	}

	@Override
	protected void processField(FormField field, Object value) {
		super.processField(field, addValue(field, value));
	}

	/**
	 * Returns a collection which contains the values that the field already has and the new values,
	 * without causing duplicates. Is called by {@link #processField(FormField, Object)}.
	 */
	protected Object addValue(FormField field, Object value) {
		Object currentValue = field.getValue();
		if (!(currentValue instanceof Collection)) {
			throw ApplicationAssertions.fail(getConfig(), ""
				+ "Add value can only applied to collection valued fields, like SelectFields."
				+ " (Otherwise, it would be an 'Update', not an 'Add'.)");
		}
		Collection<Object> currentValues = new ArrayList<>((Collection<?>) currentValue);
		Collection<Object> valuesToAdd = asList(value);
		valuesToAdd.removeAll(currentValues);
		currentValues.addAll(valuesToAdd);
		return currentValues;
	}

	private List<Object> asList(Object value) {
		if (value instanceof Collection) {
			return new ArrayList<>((Collection<?>) value);
		} else {
			return CollectionUtil.intoList(value);
		}
	}
}
