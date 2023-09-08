/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core;

import java.util.Collection;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.element.structured.StructuredElement;

    
/**
 * The AttributeValueFinder returns the first found attribute value != null.
 * 
 * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public class AttributeValueFinder implements TLElementVisitor {

    /* name of attribute to search for */
    private String maName;
    /* handle empty collections or strings as null */
    private boolean emptyIsNull;
    /* the return value */
    private Object value; 

    public AttributeValueFinder(String aMetaAttributeName) {
        this(aMetaAttributeName, true);
    }
    
    public AttributeValueFinder(String aMetaAttributeName, boolean emptyIsNull) {
        this.maName      = aMetaAttributeName;
        this.emptyIsNull = emptyIsNull;
    }
    
    @Override
	public boolean onVisit(StructuredElement anElement, int aDepth) {
        if (anElement instanceof Wrapper && this.value == null) {
            Wrapper theMandator = (Wrapper) anElement;
			if (theMandator.tType().getPart(this.maName) != null) {
                this.value = theMandator.getValue(this.maName);
                if (this.emptyIsNull && this.value != null) {
                    if (this.value instanceof Collection && CollectionUtil.isEmptyOrNull((Collection) this.value)) {
                        this.value = null;
                    }
                    else if (this.value instanceof String && StringServices.isEmpty(this.value)) {
                        this.value = null;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    public Object getValue() {
        return this.value;
    }
}

