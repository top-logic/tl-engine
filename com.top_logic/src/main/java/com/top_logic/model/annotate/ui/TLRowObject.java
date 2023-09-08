/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link TLAttributeAnnotation} that determines whether an add button to create new objects, a
 * remove button to delete object or a button to adjust the order of the rows is added to the table
 * that represents a {@link TLReference reference}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@TagName(TLRowObject.ROW_OBJECT)
@TargetType(value = TLTypeKind.COMPOSITION)
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface TLRowObject extends TLAttributeAnnotation {

	/**
	 * Tag name for {@link TLRowObject}.
	 */
	String ROW_OBJECT = "row-object";
	
	/**
	 * True if an add button to create new row objects is added to the table.
	 */
	@BooleanDefault(true)
	boolean isCreatable();

	/**
	 * True if a remove button to delete existing row objects is added to the table.
	 */
	@BooleanDefault(true)
	boolean isDeletable();

	/**
	 * True if a button to adjust the order of the rows is added to the table.
	 */
	@BooleanDefault(true)
	boolean isSortable();

}
