/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.layout.table.model.IDColumnConfig;
import com.top_logic.layout.table.model.IDColumnTableConfigurationProvider;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Sets the id column of tables representing objects of the annotated type.
 * 
 * <p>
 * The id column is the column for which an icon of the row objects type is added before its value
 * and which is rendered as a link to its row object.
 * </p>
 * 
 * <p>
 * If no id column is annotated then the <code>name</code> column is used by default, if it exists.
 * </p>
 * 
 * @see IDColumnConfig
 * @see IDColumnTableConfigurationProvider
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@TagName(IDColumnConfig.ID_COLUMN)
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@AnnotationInheritance(Policy.INHERIT)
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface TLIDColumn extends TLTypeAnnotation, TLAttributeAnnotation, IDColumnConfig {

	// Marker interface.

}
