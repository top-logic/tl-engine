/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The SingleValueReverseMetaAttributeLocator provides the single referrer to an object 
 * via a given meta attribute. 
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class SingleValueReverseMetaAttributeLocator extends ReverseMetaAttributeLocator {

	public SingleValueReverseMetaAttributeLocator(String aConfig) {
		super(aConfig);
	}
	
	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		Collection<? extends Object> values = (Collection<? extends Object>) super.locateAttributeValue(anObject);
		if (CollectionUtil.isEmptyOrNull(values)) {
			return null;
		} else {
			if (values.size() > 1) {
				TLStructuredTypePart ma = this.getMetaAttribute();
				throw new WrapperRuntimeException("Found more than one referer via me: "+AttributeOperations.getMetaElement(ma).getName()+" ma: "+ma.getName()+" for value: "+anObject);
			} else {
				return values.iterator().next();
			}
		}
	}

	@Override
	protected boolean isCollection() {
		return false;
	}

}

