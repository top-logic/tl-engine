/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.model.annotate.BooleanAnnotation;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that defines display properties of {@link String}-typed attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("multi-line")
@TargetType(value = TLTypeKind.STRING)
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface MultiLine extends BooleanAnnotation, TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * The number of rows for the displayed text area input element.
	 */
	@Name("rows")
	@IntDefault(MultiLineText.DEFAULT_ROWS)
	@DynamicMode(fun = RowsMode.class, args = @Ref(VALUE))
	int getRows();

	/**
	 * @see #getRows()
	 */
	void setRows(int value);

	/**
	 * Function activating {@link MultiLine#getRows()} depending on {@link MultiLine#getValue()}.
	 */
	public class RowsMode extends Function1<FieldMode, Boolean> {
		@Override
		public FieldMode apply(Boolean arg) {
			return arg.booleanValue() ? FieldMode.ACTIVE : FieldMode.DISABLED;
		}
	}

	/**
	 * Creates a {@link MultiLine} annotation with the rows attribute set to the given value.
	 */
	static TLAnnotation rows(int rows) {
		MultiLine result = TypedConfiguration.newConfigItem(MultiLine.class);
		result.setRows(rows);
		return result;
	}
}
