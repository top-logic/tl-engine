/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
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
 * Collection of visible columns.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@TagName(TLVisibleColumns.VISIBLE_COLUMNS)
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface TLVisibleColumns extends TLAttributeAnnotation {

	/**
	 * Tag name for {@link TLVisibleColumns}.
	 */
	public static final String VISIBLE_COLUMNS = "visible-columns";

	/**
	 * Technical names of the visible columns.
	 */
	@Options(fun = AllVisibleColumns.class, mapping = ColumnOptionMapping.class)
	@OptionLabels(value = ColumnOptionLabelProvider.class)
	@Format(CommaSeparatedStrings.class)
	List<String> getVisibleColumns();

}
