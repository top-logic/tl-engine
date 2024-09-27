/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.provider;

import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.element.meta.kbbased.WebFolderAttributeOperations;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.Resources;

/**
 * Return the name of the attribute, if the folder the value of a meta attribute.
 * 
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ZipFolderNameProvider implements LabelProvider {

    public static final LabelProvider INSTANCE = new ZipFolderNameProvider();
    
    @Override
	public String getLabel(Object object) {
        WebFolder folder = (WebFolder) object;
		TLObject owner = folder.getOwner();
        
        if (owner instanceof WebFolder) {
            return folder.getName();
        }
        else if (owner == null) {
            WebFolderAttributeOperations.ResponsibilityData data = WebFolderAttributeOperations.getResponsibilityData(folder);
            if(data != null) {
            	TLStructuredTypePart attribute = data.getMetaAttribute();
            	if (attribute != null) {
            		return MetaLabelProvider.INSTANCE.getLabel(attribute);
            	}
            }
        }
        
		return Resources.getInstance()
			.getString(WebFolderUtils.DEFAULT_WEBFOLDER_TABLE_RESOURCES.getStringResource("firstNode"));
    }

}
