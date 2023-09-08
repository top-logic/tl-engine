/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.mig.html.HTMLConstants;

/**
 * See {@link #AlignmentRenderer(Renderer, String)}.
 * 
 * @author    <a href="mailto:tdi@top-logic.com">tdi</a>
 */
public class AlignmentRenderer<T> implements Renderer<T>, HTMLConstants {

	public static final AlignmentRenderer<Object> INSTANCE_LEFT =
		new AlignmentRenderer<>(ResourceRenderer.INSTANCE, LEFT_VALUE);

	public static final AlignmentRenderer<Object> INSTANCE_CENTER =
		new AlignmentRenderer<>(ResourceRenderer.INSTANCE, CENTER_VALUE);

	public static final AlignmentRenderer<Object> INSTANCE_RIGHT =
		new AlignmentRenderer<>(ResourceRenderer.INSTANCE, RIGHT_VALUE);

	private Renderer<? super T> innerRenderer;
    private String   alignment;

    /**
     * Creates a {@link AlignmentRenderer} that writes the content with an
     * inner renderer but in an enclosing div tag with the specified alignment.
     * 
     * @param aInnerRenderer
     *            The inner renderer to write the content. Must NOT be <code>null</code>.
     * @param aAlignment
     *            The alignment for the content (e.g. right or justify).
     */
	public AlignmentRenderer(Renderer<? super T> aInnerRenderer, String aAlignment) {
        this.innerRenderer = aInnerRenderer;
        this.alignment     = aAlignment;
    }

    /**
     * @see com.top_logic.layout.Renderer#write(com.top_logic.layout.DisplayContext, com.top_logic.basic.xml.TagWriter, java.lang.Object)
     */
    @Override
	public void write(DisplayContext aContext, TagWriter aOut, T aValue) throws IOException {
        // Open the div
        aOut.beginBeginTag(DIV);
        aOut.writeAttribute(ALIGN_ATTR, this.alignment);
        aOut.endBeginTag();
        
        // Write with the inner renderer the content
        this.innerRenderer.write(aContext, aOut, aValue);
        
        // close the div
        aOut.endTag(DIV);
    }

}

