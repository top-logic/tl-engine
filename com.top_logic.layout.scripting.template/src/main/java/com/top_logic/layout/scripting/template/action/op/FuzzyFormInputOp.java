/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.action.op;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.scripting.action.FormAction;
import com.top_logic.layout.scripting.runtime.action.FormRawInputOp;
import com.top_logic.layout.scripting.template.excel.format.FlexibleBooleanFormat;
import com.top_logic.layout.scripting.template.excel.format.FlexibleDateFormat;
import com.top_logic.layout.scripting.template.fuzzy.FlexibleFormatKind;
import com.top_logic.layout.scripting.template.fuzzy.FormFuzzyInput;

/**
 * A {@link FormRawInputOp} that uses the {@link FlexibleDateFormat} and
 * {@link FlexibleBooleanFormat} for parsing the value for {@link ComplexField}s with a
 * {@link DateFormat} and {@link BooleanField}s.
 * 
 * @deprecated Use {@link FormFuzzyInput}
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@Deprecated
public class FuzzyFormInputOp extends FormRawInputOp {

	/**
	 * Creates a {@link FuzzyFormInputOp} from a {@link FormAction}.
	 * <p>
	 * Is called by the {@link TypedConfiguration}.
	 * </p>
	 */
	@CalledByReflection
	public FuzzyFormInputOp(InstantiationContext context, FormAction config) {
		super(context, config);
	}

	@Override
	protected void processField(FormField field, Object value) {
		try {
			super.processField(field, fuzzyTransformValue(field, value));
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Object fuzzyTransformValue(FormField field, Object value) throws ParseException {

		if (field instanceof BooleanField) {
			return FlexibleFormatKind.BOOLEAN.parseValue((String) value);
		}
		else if (field instanceof ComplexField) {
			ComplexField withFormat = (ComplexField) field;
			Format fieldFormat = withFormat.getFormat();
			if (fieldFormat instanceof DateFormat) {
				Object fuzzyDate = FlexibleFormatKind.DATE.parseValue((String) value);
				return fieldFormat.format(fuzzyDate);
			}
		}

		return value;
	}

}
