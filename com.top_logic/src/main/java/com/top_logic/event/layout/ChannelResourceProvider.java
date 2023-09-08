/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import java.util.Iterator;

import com.top_logic.basic.xml.TagUtil;
import com.top_logic.event.bus.Channel;
import com.top_logic.event.bus.IReceiver;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The ChannelResourceProvider provides label an tooltips for a {@link Channel}
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ChannelResourceProvider extends DefaultResourceProvider {

    /**
	 * Creates a {@link ChannelResourceProvider}.
	 */
    public ChannelResourceProvider() {
        super();
    }
    
    /**
     * @see com.top_logic.mig.html.DefaultResourceProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object aObject) {
        if (aObject instanceof Channel) {
            Channel theChannel = (Channel) aObject;
            return  "(" + theChannel.getService().getNameSpace() + "," + theChannel.getService().getName() +")";
        }
        return super.getLabel(aObject);
    }
    
    /**
     * @see com.top_logic.mig.html.DefaultResourceProvider#getTooltip(java.lang.Object)
     */
    @Override
	public String getTooltip(Object aObject) {
        if (aObject instanceof Channel) {
            Channel theChannel = (Channel) aObject;
			StringBuffer tooltip = new StringBuffer(256);
			tooltip.append("Receiver:");
			TagUtil.beginBeginTag(tooltip, HTMLConstants.BR);
			TagUtil.endEmptyTag(tooltip);
			for (Iterator<IReceiver> theRecs = theChannel.getReceiverList().iterator(); theRecs.hasNext();) {
				IReceiver theRec = theRecs.next();
				TagUtil.beginTag(tooltip, HTMLConstants.BOLD);
				tooltip.append(theRec.getClass());
				TagUtil.endTag(tooltip, HTMLConstants.BOLD);
				TagUtil.beginBeginTag(tooltip, HTMLConstants.BR);
				TagUtil.endEmptyTag(tooltip);
            }
			return tooltip.toString();

        }
        return super.getLabel(aObject);
    }
}

