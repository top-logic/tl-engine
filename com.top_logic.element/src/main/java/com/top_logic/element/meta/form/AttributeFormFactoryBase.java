/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;


import com.top_logic.basic.Logger;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Base class for {@link AttributeFormFactory} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AttributeFormFactoryBase extends AttributeFormFactory {
	
	protected Object getDefaultValue(Wrapper anAttributed, TLStructuredTypePart theMetaAttribute, boolean useDefault) {
		Object theDefaultValue = null;
		try {
            if (anAttributed != null) {
                theDefaultValue = anAttributed.getValue(theMetaAttribute.getName());
            }
		}
		catch (Exception ex) {
			Logger.warn ("Cannot get value for attribute " + theMetaAttribute.getName() + " of object " + anAttributed, ex, this);
		}
		return theDefaultValue;
	}

}