/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user.douser;

import com.top_logic.basic.xml.TagUtil;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class DOUserResourceProvider extends DefaultResourceProvider {


    /**
     * @see com.top_logic.layout.ResourceProvider#getTooltip(java.lang.Object)
     */
    @Override
	public String getTooltip(Object aObject) {
        if (aObject instanceof DOUser) {
            DOUser theUser = (DOUser) aObject;
            StringBuffer buffer = new StringBuffer(64);
            buffer.append("Name:");
			TagUtil.beginTag(buffer, HTMLConstants.BOLD);
			TagUtil.writeText(buffer, theUser.getLastName());
			buffer.append(", ");
			TagUtil.writeText(buffer, theUser.getFirstName());
			buffer.append(" (");
			TagUtil.writeText(buffer, theUser.getUserName());
			buffer.append(")");
			TagUtil.endTag(buffer, HTMLConstants.BOLD);
			TagUtil.beginBeginTag(buffer, HTMLConstants.BR);
			TagUtil.endEmptyTag(buffer);
            return buffer.toString();
        }
        return super.getTooltip(aObject);
    }

    /**
     * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object aObject) {
        if (aObject instanceof DOUser) {
            DOUser theUser = (DOUser) aObject;
            return theUser.getLastName() + ", " + theUser.getFirstName() + " (" + theUser.getUserName() + ")"; 
        }
        return super.getLabel(aObject);
    }

}

