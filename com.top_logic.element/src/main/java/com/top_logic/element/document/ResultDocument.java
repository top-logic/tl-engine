/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.document;

import com.top_logic.element.meta.kbbased.AttributedDocument;
import com.top_logic.element.meta.kbbased.WebFolderAttributeOperations;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * Document with an optional Template Document
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class ResultDocument extends AttributedDocument {

	public ResultDocument(KnowledgeObject ko) {
		super(ko);
	}
	
	/**
	 * The security parent is the Wrapper to which it is attached via a {@link TLStructuredTypePart}. Falls
	 * back to the super implementation if it is not attached to a Wrapper.
	 * 
	 * @see com.top_logic.tool.boundsec.BoundObject#getSecurityParent()
	 */
	@Override
	public BoundObject getSecurityParent() {
		TLObject theResp = null;
		WebFolderAttributeOperations.ResponsibilityData theData = WebFolderAttributeOperations.getResponsibilityData(this);
		if (theData != null) {
			theResp = theData.getResponsibleObject();
		}        
		
		if (theResp instanceof BoundObject) {
	        return (BoundObject) theResp;
	    }
	    else {
	        return super.getSecurityParent();
	    }
	}
}
