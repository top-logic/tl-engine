/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Dependency that only accepts if all dependent fields contain a value or none of it.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class AllOrNothingDependency extends AbstractDependency {

	/**
	 * @see AbstractDependency#AbstractDependency(FormField[])
	 */
	public AllOrNothingDependency(FormField[] relatedFields) {
		super(relatedFields);
	}

	/**
	 * @see AbstractDependency#AbstractDependency(FormField[], boolean)
	 */
	public AllOrNothingDependency(FormField[] relatedFields, boolean asWarning) {
		super(relatedFields, asWarning);
	}

	@Override
	protected boolean check(int checkedFieldIndex, Object value) throws CheckException {
		boolean fieldWithValueFound = false;
		boolean fieldWithoutValueFound = false;
		for (int i = 0, cnt = size(); i < cnt; i++) {
			FormField dependency = get(i);

			if (dependency.hasValue()) {
				Object fieldValue = dependency.getValue();
				if (fieldValue instanceof Collection) {
					if (!CollectionUtil.isEmptyOrNull((Collection) fieldValue)) {
						fieldWithValueFound = true;
					} else {
						fieldWithoutValueFound = true;
					}
				} else {
					if (fieldValue != null && !fieldValue.equals(StringServices.EMPTY_STRING)) {
						fieldWithValueFound = true;
					} else {
						fieldWithoutValueFound = true;
					}
				}
			} else {
				// check could not be run because some fields don't contain a value
				return false;
			}
		}

		if (fieldWithValueFound && fieldWithoutValueFound) {
			// don't accept if some fields contain a value and others don't
			throw new CheckException(createErrorMessage());
		}

		// accept if there was a value found either for all dependent fields or none of it
		return true;
	}

	/**
	 * Create the error message for the check exception.
	 */
	protected String createErrorMessage() {
		return Resources.getInstance().getString(I18NConstants.ALL_OR_NOTHING_MESSAGE);
	}

}
