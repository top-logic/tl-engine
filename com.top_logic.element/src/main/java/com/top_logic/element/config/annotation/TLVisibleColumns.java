/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import java.util.Set;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.element.layout.table.AllVisibleColumns;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * All available columns a user can select from.
 * 
 * <p>
 * If this annotation is given on a reference, it has only an effect, if the reference is displayed
 * as a table in a form.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@TagName(TLVisibleColumns.VISIBLE_COLUMNS)
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
@Label("Available columns")
public interface TLVisibleColumns extends TLAttributeAnnotation {

	/**
	 * Tag name for {@link TLVisibleColumns}.
	 */
	public static final String VISIBLE_COLUMNS = "visible-columns";

	/**
	 * Technical names of the columns that are available in a table derived from the annotated model
	 * element.
	 */
	@Options(fun = AllVisibleColumns.class, mapping = ColumnOptionMapping.class)
	@OptionLabels(value = ColumnOptionLabelProvider.class)
	@Format(CommaSeparatedStringSet.class)
	@Label("Columns")
	Set<String> getVisibleColumns();

}
