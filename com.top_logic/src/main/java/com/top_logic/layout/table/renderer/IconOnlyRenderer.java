/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Renderer using the meta resource provider to write an icon with tooltip and
 * goto link for the given object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class IconOnlyRenderer implements Renderer<Object> {

    public static IconOnlyRenderer INSTANCE = new IconOnlyRenderer(true);

    public static IconOnlyRenderer INSTANCE_NO_TOOLTIP = new IconOnlyRenderer(false);

    private final boolean withTooltip;

    /** 
     * Creates a {@link IconOnlyRenderer}.
     */
    public IconOnlyRenderer(boolean withTooltip) {
        this.withTooltip = withTooltip;
    }

    /**
     * @see com.top_logic.layout.Renderer#write(com.top_logic.layout.DisplayContext, com.top_logic.basic.xml.TagWriter, java.lang.Object)
     */
    @Override
	public void write(DisplayContext aContext, TagWriter aOut, Object aValue) throws IOException {
		TagWriter theWriter = aOut;
		ThemeImage theImage;
        String     theText;
        String     theURL;
		String cssClass;
        boolean    hasURL;
		boolean hasCssClass;

        if (aValue == null) {
			theImage = Icons.EMPTY;
            theText  = "";
            theURL   = null;
            hasURL   = false;
			cssClass = null;
			hasCssClass = false;
        }
        else {
            theImage = MetaResourceProvider.INSTANCE.getImage(aValue, Flavor.DEFAULT);
            theText  = this.withTooltip ? MetaResourceProvider.INSTANCE.getLabel(aValue) : null;
            theURL   = MetaResourceProvider.INSTANCE.getLink(aContext, aValue);
            hasURL   = !StringServices.isEmpty(theURL);
			cssClass = MetaResourceProvider.INSTANCE.getCssClass(aValue);
			hasCssClass = cssClass != null;
        }

        if (hasURL) {
			HTMLUtil.beginA(theWriter, "javascript:" + theURL, null, null, cssClass);
		}
		else if (hasCssClass) {
			theWriter.beginBeginTag(HTMLConstants.SPAN);
			theWriter.writeAttribute(HTMLConstants.CLASS_ATTR, cssClass);
			theWriter.endBeginTag();
        }
		theImage.writeWithPlainTooltip(aContext, aOut, theText);
        if (hasURL) {
            HTMLUtil.endA(theWriter);
        }
		else if (hasCssClass) {
			theWriter.endTag(HTMLConstants.SPAN);
		}
    }
}
