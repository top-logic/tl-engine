/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.layout.table.model.SortColumnsConfig;
import com.top_logic.layout.table.model.SortColumnsTableConfigurationProvider;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Sets the sort order of tables representing objects of the annotated type.
 * 
 * @see SortColumnsConfig
 * @see SortColumnsTableConfigurationProvider
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@TagName(SortColumnsConfig.SORT_COLUMNS)
public interface TLSortColumns extends TLTypeAnnotation, TLAttributeAnnotation, SortColumnsConfig {

	// Marker interface.

}
