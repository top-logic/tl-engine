/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config.annotation;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Annotation for an {@link TLEnumeration} to define whether the elements are un-ordered, i.e.
 * whether the definition order of the classifier is not relevant.
 * 
 * @see TLEnumeration#getClassifiers()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("unordered-enum")
@InApp
@TargetType(TLTypeKind.ENUMERATION)
public interface UnorderedEnum extends TLTypeAnnotation {

	// Marker annotation.

}

