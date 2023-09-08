/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.AbstractControlRenderer;


/**
 * {@link Renderer} that defines the visual appearance of {@link Control}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DefaultControlRenderer<T extends Control> extends AbstractControlRenderer<T> {

    public DefaultControlRenderer() {
    	// Configured in sub classes
    }

    /**
     * Adaption to the {@link Renderer} interface. 
     */
    @Override
	public final void write(DisplayContext context, TagWriter out, T value) throws IOException {
		writeControl(context, out, value);
    }
    
    /**
	 * Renders the given control.
	 * <p>
	 * Final to define the contract that a {@link Control} is rendered within a single HTML element
	 * that is identified with the {@link Control#getID() control ID}.
	 * </p>
	 */
	public final void writeControl(DisplayContext context, TagWriter out, T control) throws IOException {
		String contentTag = getControlTag(control);

		out.beginBeginTag(contentTag);
		if (control.isVisible()) {
			writeControlTagAttributes(context, out, control);
		} else {
			out.writeAttribute(ID_ATTR, control.getID());
			out.writeAttribute(STYLE_ATTR, "display:none");
		}
		out.endBeginTag();
		if (control.isVisible()) {
			writeControlContents(context, out, control);
		}
		out.endTag(contentTag);
	}

	/**
	 * The HTML tag name of the control element rendered by this renderer.
	 * 
	 * <p>
	 * Example: If this renderer renders the control as 
	 * 	<blockquote> 
	 * 		<xmp> 
	 * 			<div id="..." class="...">
	 * 				...
	 * 			</div>
	 * 		</xmp> 
	 * 	</blockquote>
	 * 
	 * 	then, "<code>div</code>" is returned.
	 * </p>
	 */
	protected abstract String getControlTag(T control);
    
	/**
	 * Renders the contents of the given {@link Control}. 
	 */
	protected abstract void writeControlContents(DisplayContext context, TagWriter out, T control) throws IOException;
}
