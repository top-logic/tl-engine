/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.document;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * No not allow execution in case an {@link TLObject} contains non-empty {@link WebFolder}s.
 * 
 * @author <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class HasDocumentsExecutabilityRule implements ExecutabilityRule {

    public static final HasDocumentsExecutabilityRule INSTANCE = new HasDocumentsExecutabilityRule();
    
    private HasDocumentsExecutabilityRule() {
        // enforce usage of INSTANCE
    }
    
    /** 
     * Allow only in case the underlying Model has only empty WebFolder.
     */
    @Override
	public ExecutableState isExecutable(LayoutComponent component,  Object model, Map<String, Object> someValues) {
		Wrapper attr = (Wrapper) model;
        if((attr == null) || !attr.tValid()) {
        	return ExecutableState.NO_EXEC_NO_MODEL;
        }
		TLStructuredType type = attr.tType();
		List<TLStructuredTypePart> folderParts = AttributeOperations.getWebFolderAttributes(type);
		if (folderParts.isEmpty()) {
			Logger.warn("Type " + type + " has no Attributes of TYPE_WEBFOLDER", this);
            return ExecutableState.EXECUTABLE;
        }
		{
        	boolean debugEnabled = Logger.isDebugEnabled(HasDocumentsExecutabilityRule.class);
			for (TLTypePart part : folderParts) {
				WebFolder theWF = (WebFolder) attr.getValue(part.getName());
                if (theWF == null) {
                	if (debugEnabled) {
						Logger.debug("No webfolder found for '" + part.getName() + "' in '" + attr + "', ignored.",
							HasDocumentsExecutabilityRule.class);
                	}
                	continue;
                }
                
                if (!theWF.isEmpty()) {
                    return ExecutableState.createDisabledState(I18NConstants.HAS_NON_EMPTY_WEBFOLDERS);
                }
            }
        }
        return ExecutableState.EXECUTABLE;        
    }

}
