/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.Accessor;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Utils;

/**
 * {@link Accessor} implementation that provides "reflective" access to implementations of
 * {@link TLObject}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class WrapperAccessor implements Accessor<Object> {

	/**
	 * Name for an "artificial" property of a {@link TLObject} object that is mapped to the
	 * {@link TLObject}s {@link KBUtils#getWrappedObjectName(TLObject)}.
	 */
	public static final String IDENTIFIER_PROPERTY = BasicTypes.IDENTIFIER_ATTRIBUTE_NAME;

    public static final String SELF = "_self";

	/**
	 * Singleton instance of this class.
	 */
	public static final WrapperAccessor INSTANCE = new WrapperAccessor();
	
	/**
	 * Singleton constructor.	 
	 */
	public WrapperAccessor() {
		super();
	}
	
	@Override
	public Object getValue(Object object, String property) {
		if (!(object instanceof TLObject)) {
			return null;
		}
		TLObject theWrapper = (TLObject) object;
        if (!theWrapper.tValid()) {
            return null;
        }

        if (IDENTIFIER_PROPERTY.equals(property)) {
            return KBUtils.getWrappedObjectName(theWrapper);
        }
        else if (property.indexOf('.') > 0) {
            return Utils.getValueByPath(property, theWrapper);
        }
        else {
			TLStructuredType type = theWrapper.tType();
			if (type != null) {
				TLStructuredTypePart part = type.getPart(property);
				if (part != null) {
					return theWrapper.tValue(part);
				}
			}
			// Legacy support for undefined attributes such as "wrapper" "name" e.g. in
			// structure roots.
			return theWrapper.tGetData(property);
        }
	}

	@Override
	public void setValue(Object object, String property, Object value) {
		TLObject theWrapper = (TLObject) object;
        if (IDENTIFIER_PROPERTY.equals(property)) {
            throw new UnsupportedOperationException();
        } 
        else {
			theWrapper.tUpdateByName(property, value);
        }
	}

}
