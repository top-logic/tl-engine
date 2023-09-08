/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link TLAttributeAnnotation} that customizes the search relevance of attributes.
 * 
 * <p>
 * Note: If such annotation is absent, a type-specific default applies.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName(TLSearchRelevant.TAG_NAME)
public interface TLSearchRelevant extends TLAttributeAnnotation, BooleanAnnotation {

	/** Custom tag to create a {@link TLSearchRelevant} annotation. */
	String TAG_NAME = "search-relevant";

}

