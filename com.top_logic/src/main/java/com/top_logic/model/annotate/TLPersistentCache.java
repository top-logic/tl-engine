/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * This annotation determines if the values of the annotated object is actually a persistent cache.
 * 
 * <p>
 * When the annotated element is a {@link TLStructuredType}, then instances of this class are used
 * as a persistent cache, e.g. for performance reasons.
 * </p>
 * 
 * <p>
 * When the annotated element is a {@link TLStructuredTypePart}, the value of the attribute is used
 * as a persistent cache, e.g. for performance reasons.
 * </p>
 * 
 * <p>
 * In all these cases, the application automatically changes the value and it must not be changed by
 * the user.
 * </p>
 */
@TagName(TLPersistentCache.TAG_NAME)
@Label("Persistent cache")
public interface TLPersistentCache extends TLAttributeAnnotation, TLTypeAnnotation, BooleanAnnotation {

	/** Custom tag to create a {@link TLPersistentCache} annotation. */
	String TAG_NAME = "persistent-cache";

}
