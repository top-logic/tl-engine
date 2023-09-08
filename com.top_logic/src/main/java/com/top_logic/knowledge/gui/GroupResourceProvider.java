/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui;

import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.tool.boundsec.wrap.GroupResourceHelper;
import com.top_logic.util.Resources;

/**
 * Resource provider for groups.
 *
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class GroupResourceProvider extends WrapperResourceProvider {

    public static final GroupResourceProvider INSTANCE = new GroupResourceProvider();

    @Override
	public String getLabel(Object anObject) {
        if (anObject instanceof Group) {
            try {
                Group theGroup     = (Group) anObject;
                Resources   theRes    = Resources.getInstance();
				return theRes.getString(GroupResourceHelper.getResKey(theGroup));
            } catch (Exception ex) {
                return super.getLabel(anObject);
            }
        }
        return super.getLabel(anObject);
    }

    @Override
    public String getLink(DisplayContext context, Object object) {
        // Don't show links to representative groups
        if (object instanceof Group && ((Group)object).isRepresentativeGroup()) {
            return null;
        }
        return super.getLink(context, object);
    }

}
