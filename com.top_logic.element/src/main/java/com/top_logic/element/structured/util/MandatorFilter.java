/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.AbstractAttributedValueFilter;
import com.top_logic.element.structured.wrap.Mandator;

/**
 * Accept Mandators
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
@Deprecated
public class MandatorFilter extends AbstractAttributedValueFilter {

	/** 
	 * Create a new MandatorFilter
	 */
	public MandatorFilter() {
		super();
	}

	/**
     * @see com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter#accept(Object, EditContext)
     */
    @Override
	public boolean accept(Object anObject, EditContext anAttributeUpdateContainer) {
    	return (anObject instanceof Mandator);
	}
}
