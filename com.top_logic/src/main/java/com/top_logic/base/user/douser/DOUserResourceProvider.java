/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.user.douser;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * 
 * {@link ResourceProvider} for {@link DOUser}s.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class DOUserResourceProvider extends DefaultResourceProvider {

    @Override
	public String getTooltip(Object aObject) {
        if (aObject instanceof DOUser) {
            DOUser theUser = (DOUser) aObject;
            StringBuffer buffer = new StringBuffer(64);
            buffer.append("Name:");
			TagUtil.beginTag(buffer, HTMLConstants.BOLD);
			TagUtil.writeText(buffer, theUser.getName());
			String firstName = theUser.getFirstName();
			if (!StringServices.isEmpty(firstName)) {
				buffer.append(", ");
				TagUtil.writeText(buffer, firstName);
			}
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

    @Override
	public String getLabel(Object aObject) {
        if (aObject instanceof DOUser) {
            DOUser theUser = (DOUser) aObject;
			StringBuilder label = new StringBuilder();
			label.append(theUser.getName());
			String firstName = theUser.getFirstName();
			if (!StringServices.isEmpty(firstName)) {
				label.append(", ").append(firstName);
			}
			label.append(" (").append(theUser.getUserName()).append(")");
			return label.toString();
        }
        return super.getLabel(aObject);
    }

}

