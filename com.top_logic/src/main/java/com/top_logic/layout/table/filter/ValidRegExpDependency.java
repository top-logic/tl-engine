/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.AbstractDependency;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.util.Resources;

/**
 * Dependency, to ensure a valid regular expression pattern.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
class ValidRegExpDependency extends AbstractDependency {


	ValidRegExpDependency(BooleanField regExpTrigger, StringField textField) {
		super(new FormField[] { regExpTrigger, textField });
	}

	@Override
	protected boolean check(int checkedFieldIndex, Object value) throws CheckException {
		if (get(checkedFieldIndex) instanceof StringField) {
			BooleanField regExpTrigger = (BooleanField) get((checkedFieldIndex + 1) % 2);
			if (regExpTrigger.hasValue()) {
				try {
					if (regExpTrigger.getAsBoolean()) {
						Pattern.compile((String) value);
					}
					return true;
				} catch (PatternSyntaxException e) {
					String theMessage =
						Resources.getInstance().getString(TextFilterView.INVALID_REG_EXP_EXPRESSION);
					throw new CheckException(theMessage);
				}
			}

			return false;
		}

		return true;
	}
}