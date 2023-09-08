/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.basic.Logger;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * Check that Document lies in a WebFolder of the current object.
 * 
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 */
@Deprecated
public class CurrentAttributedDocumentFilter extends AbstractAttributedValueFilter {

	/** 
	 *Create a new CurrentAttributedDocumentFilter
	 */
	public CurrentAttributedDocumentFilter() {
		super();
	}

	@Override
	public boolean accept(Object anObject, EditContext editContext) {
		if (!(anObject instanceof Document)) {
			return false;
		}
		
		try {
			WebFolder theFolder = WebFolder.getWebFolder(((Document) anObject));
			if (theFolder != null) { // nobody knows...
				BoundObject theSec = theFolder.getSecurityParent();
				TLObject object = editContext.getObject();
				if (theSec == null) {
					return object == null;
				}
				
				return theSec.equals(object); // == should do as these should be Wrappers...
												// Anyway...
			}
		}
		catch (Exception ex) {
			Logger.warn("Failed to filter Document: " + anObject, ex ,this);
		}
		
		return false;
	}

}
