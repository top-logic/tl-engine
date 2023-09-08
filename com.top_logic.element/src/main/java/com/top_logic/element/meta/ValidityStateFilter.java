/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredTypePart;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ValidityStateFilter implements Filter {

    private Collection state;
    private TLStructuredTypePart metaAttribute;

    public ValidityStateFilter(TLStructuredTypePart aMA, Collection aState) {
        this.metaAttribute = aMA;
        this.state         = aState;
    }

    public ValidityStateFilter(TLStructuredTypePart aMA, String aState) {
        this(aMA, Collections.singletonList(aState));
    }

    @Override
	public boolean accept(Object anObject) {
        if (this.state == null) {
            return (true);
        }
        else {
            String theState = WrapperMetaAttributeUtil.getValidityState((Wrapper) anObject, this.metaAttribute);

            return (this.state.contains(theState));
        }
    }
}
