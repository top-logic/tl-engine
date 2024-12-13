/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.annotate.LabelPositionAnnotation;

/**
 * {@link Annotation} for {@link PropertyDescriptor}s to annotate a {@link LabelPosition}.
 * 
 * @see LabelPositionAnnotation Corresponding annotation for {@link TLTypePart} and {@link TLType}.
 */
@Retention(RUNTIME)
@TagName("label-position")
public @interface LabelPositioning {

	/**
	 * The positon where to place the label relative to the value.
	 */
	LabelPosition value();

}

