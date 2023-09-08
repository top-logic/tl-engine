/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * Integrate WebFolder into COS security giving it a security parent.
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class WebFolderAsAttribute extends AttributedWebFolder {

    public WebFolderAsAttribute(KnowledgeObject ko) {
        super(ko);
    }
    
    /**
     * The security parent is the Wrapper to which its top level
     * WebFolder is attached via a WebFolderMetaAttribute.
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
    
    /**
     * Get the WebFolder type, i.e. the name of the WebFolderMetaAttribute
     * with which the top level folder of this one is attached to
     * the security parent.
     * 
     * @see com.top_logic.knowledge.wrap.WebFolder#getFolderType()
     */
    @Override
	public String getFolderType() {
    	WebFolderAttributeOperations.ResponsibilityData theData = WebFolderAttributeOperations.getResponsibilityData(this);
    	if (theData != null) {
    		return theData.getWebFolderType();
    	}
    	
        return null;
    }

}
