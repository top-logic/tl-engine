/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.StringAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link TLAttributeAnnotation} specifying a periodic validity check for an attribute.
 * 
 * @see #getValue()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(TLValidityCheck.TAG_NAME)
public interface TLValidityCheck extends StringAnnotation, TLAttributeAnnotation {

	/**
	 * Custom tag to create a {@link TLValidityCheck} annotation.
	 */
	String TAG_NAME = "validity";

	/**
	 * A specification of the validity time.
	 */
	@Override
	String getValue();

}
