/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query.kbbased;

import java.util.List;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.element.meta.query.CollectionFilter;
import com.top_logic.element.meta.query.CollectionFilterFactory;
import com.top_logic.element.meta.query.WrapperValuedAttributeFilter;

/**
 * Special handling for wrapper attribute values.
 *
 * @author    <a href="mailto:kbu@top-logic.com"></a>
 */
public class KBBasedFilterFactory extends CollectionFilterFactory {

    /**
     * Default CTor
     */
    public KBBasedFilterFactory() {
        super();
    }

    /**
     * Special handling for wrapper attribute values.
     *
     * @see com.top_logic.element.meta.query.CollectionFilterFactory#getFilter(com.top_logic.model.TLStructuredTypePart, java.lang.Object, boolean, boolean, boolean)
     */
    @Override
    protected CollectionFilter createFilter(TLStructuredTypePart anAttribute, Object aValue, boolean doNegate, boolean searchForEmptyValues, String anAccessPath) {
		switch (AttributeOperations.getMetaAttributeType(anAttribute)){
			case LegacyTypeCodes.TYPE_CLASSIFICATION:
			case LegacyTypeCodes.TYPE_WRAPPER:
			case LegacyTypeCodes.TYPE_SINGLE_REFERENCE:
			case LegacyTypeCodes.TYPE_LIST:
			case LegacyTypeCodes.TYPE_SINGLEWRAPPER:
			case LegacyTypeCodes.TYPE_TYPEDSET: {
				List theVal = asListValue(aValue, searchForEmptyValues);
				if (theVal == null) {
					return null;
				}
				
				if (AttributeOperations.isReadOnly(anAttribute)) {
					return new WrapperValuedAttributeFilter(anAttribute, theVal, doNegate, true, anAccessPath);
				} else {
					return new KBBasedWrapperValuedAttributeFilter(anAttribute, theVal, doNegate, true, anAccessPath);
				}
			}
		}

        return super.createFilter(anAttribute, aValue, doNegate, searchForEmptyValues, anAccessPath);
    }

}
