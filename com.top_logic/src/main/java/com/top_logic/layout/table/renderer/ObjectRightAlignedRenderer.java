/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Write an object within a div, which aligns it right.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ObjectRightAlignedRenderer implements Renderer<Object> {

    /** The default instance of this renderer. */ 
    public static final ObjectRightAlignedRenderer INSTANCE = new ObjectRightAlignedRenderer(MetaLabelProvider.INSTANCE);

    /** The renderer used for providing the text to be aligned. */
    private final LabelProvider innerProvider;

    /** 
     * Creates a {@link ObjectRightAlignedRenderer}.
     */
    public ObjectRightAlignedRenderer(LabelProvider anInner) {
        this.innerProvider = anInner;
    }

    /**
     * @see com.top_logic.layout.Renderer#write(com.top_logic.layout.DisplayContext, com.top_logic.basic.xml.TagWriter, java.lang.Object)
     */
    @Override
	public void write(DisplayContext aContext, TagWriter aOut, Object aValue) throws IOException {
        if (aValue != null) {
            aOut.beginBeginTag(HTMLConstants.DIV);
            aOut.writeAttribute(HTMLConstants.CLASS_ATTR, "right");
            aOut.endBeginTag();
            aOut.writeContent(getText(aValue));
            aOut.endTag(HTMLConstants.DIV);
        }
    }

    /** 
     * Convert the given value to a string value.
     * 
     * @param    aValue    The object to be converted, must not be <code>null</code>.
     * @return   The requested string representation, must not be <code>null</code>.
     */
    protected String getText(Object aValue) {
        return (this.innerProvider.getLabel(aValue));
    }
}
