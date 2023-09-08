/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAttributeAnnotation} that customizes the full-text-search relevance of attributes.
 * 
 * <p>
 * Note: If such annotation is absent, a type-specific default applies.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(TLFullTextRelevant.TAG_NAME)
@InApp
public interface TLFullTextRelevant extends BooleanAnnotation, TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Custom tag to create a {@link TLFullTextRelevant} annotation.
	 */
	String TAG_NAME = "fulltext-relevant";

}
