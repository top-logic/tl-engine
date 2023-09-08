/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;
import com.top_logic.basic.StringServices;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.knowledge.gui.WrapperResourceProvider;

/**
 * Special mandator handling (write ORG id).
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class MandatorResourceProvider extends WrapperResourceProvider {

    public static final MandatorResourceProvider MANDATOR_INSTANCE = new MandatorResourceProvider();

    /** 
	 * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object aNode) {
		if (aNode instanceof Mandator) {
			Mandator theMandator = (Mandator) aNode;
			String theLabel = (String) theMandator.getValue(Mandator.NUMBER_HANDLER_ID);
			if (! StringServices.isEmpty(theLabel)) {
			    theLabel += " - ";
			}
			theLabel += theMandator.getName();
			
			return theLabel;
		}

		return super.getLabel(aNode);
	}
}
