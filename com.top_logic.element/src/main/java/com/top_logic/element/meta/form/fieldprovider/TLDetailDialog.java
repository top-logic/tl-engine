/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.SizeInfo;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link TLAttributeAnnotation} specifying that a detail dialog should be available when editing
 * the annotated composition in a form table.
 * 
 * <p>
 * If this annotation is given, a detail dialog can be opened from a table that displays composition
 * reference contents within a form of the container.
 * </p>
 * 
 * <p>
 * The form displayed in the detail dialog is the default form for the content type.
 * </p>
 */
@InApp
@TargetType(value = TLTypeKind.COMPOSITION)
@Label("Detail dialog")
public interface TLDetailDialog extends TLAttributeAnnotation, SizeInfo {
	/**
	 * Title of the detail dialog.
	 * 
	 * <p>
	 * If not specified, a default title is used.
	 * </p>
	 * 
	 * <p>
	 * The text may contain the placeholder "{0}" that is replaced with the name of the type of the
	 * concrete object being edited.
	 * </p>
	 */
	@Label("Dialog title")
	@Override
	ResKey getDefaultI18N();
}
