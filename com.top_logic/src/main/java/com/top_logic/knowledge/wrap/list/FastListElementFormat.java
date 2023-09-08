/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.list;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.util.Resources;


/**
 * Formatter for elements from a fast list.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class FastListElementFormat extends Format {

    /**
     * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
     */
    @Override
	public Object parseObject(String aSource, ParsePosition aPos) {
        return null;
    }

    /**
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     */
    @Override
	public StringBuffer format(Object anObject, StringBuffer aBuffer, FieldPosition aPos) {
        try {
            FastListElement theElement = (FastListElement) anObject;

			aBuffer.append(Resources.getInstance().getString(TLModelNamingConvention.classifierKey(theElement)));
        }
        catch (WrapperRuntimeException ex) {
            Logger.warn("Unable to format " + anObject, ex, this);
        }

        return (aBuffer);
    }
}
