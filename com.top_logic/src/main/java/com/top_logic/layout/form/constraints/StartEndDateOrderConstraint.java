/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.util.Resources;

/**
 * Checks that start date is set before end date.
 * 
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class StartEndDateOrderConstraint implements Constraint {

	FormField _other;
	boolean _isStart;

	/**
	 * Creates a new {@link StartEndDateOrderConstraint}.
	 */
	public StartEndDateOrderConstraint(FormField other, boolean isStart) {
		_other = other;
		_isStart = isStart;
	}

	@Override
	public boolean check(Object value) throws CheckException {
		Date otherDate = (Date)_other.getValue();
		if(value!=null && otherDate!=null){
			int compResult = DateUtil.compareDatesByDay((Date)value, otherDate);
			if((_isStart && compResult > 0) || (!_isStart && compResult<0)){
				throw new CheckException(Resources.getInstance().getString(I18NConstants.START_END_DATE_ORDER_MISMATCH));
			}
		}
		return true;
	}

	@Override
	public Collection<FormField> reportDependencies() {
		return Collections.singletonList(_other);
	}

}
