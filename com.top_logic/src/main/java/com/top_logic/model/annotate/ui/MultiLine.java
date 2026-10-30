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

	/** Configuration name of {@link #getRows()}. */
	String ROWS = "rows";

	/** Configuration name of {@link #isPreformatted()}. */
	String PREFORMATTED = "preformatted";

	/**
	 * The number of rows for the displayed text area input element.
	 */
	@Name(ROWS)
	@IntDefault(MultiLineText.DEFAULT_ROWS)
	@DynamicMode(fun = RowsMode.class, args = @Ref(VALUE))
	int getRows();

	/**
	 * @see #getRows()
	 */
	void setRows(int value);

	/**
	 * Whether the text is displayed as it is written: in a font of fixed width, each line as one
	 * line without wrapping it, a line wider than the field being scrolled horizontally.
	 *
	 * <p>
	 * Suitable for text whose layout carries meaning, such as a stack trace, a log or source code.
	 * </p>
	 */
	@Name(PREFORMATTED)
	@DynamicMode(fun = RowsMode.class, args = @Ref(VALUE))
	boolean isPreformatted();

	/**
	 * @see #isPreformatted()
	 */
	void setPreformatted(boolean value);

	/**
	 * Function activating the display properties {@link MultiLine#getRows()} and
	 * {@link MultiLine#isPreformatted()} depending on {@link MultiLine#getValue()}.
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
