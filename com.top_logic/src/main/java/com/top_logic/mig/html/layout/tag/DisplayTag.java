/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;

import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.tag.AbstractTag;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * Display an object in a JSP page (using the ResourceProvider).
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DisplayTag extends AbstractTag {

    /** The object to be displayed, never <code>null</code>. */
    private Object object;

    /** Flag, if the image representing the object should be written. */
    private boolean hideImage;

    /** Destination for the goto link, if it is a wrapper object. */
    private String destination;

    /**
     * @see com.top_logic.layout.form.tag.AbstractTag#startElement()
     */
    @Override
	protected int startElement() throws JspException, IOException {
		Renderer<Object> theRenderer = ResourceRenderer.newResourceRenderer(MetaResourceProvider.INSTANCE,
			ResourceRenderer.USE_LINK, ResourceRenderer.USE_TOOLTIP, !this.hideImage);

        theRenderer.write(DefaultDisplayContext.getDisplayContext(this.pageContext), this.out(), this.object);

        return SKIP_BODY;
    }

    /**
     * @see com.top_logic.layout.form.tag.AbstractTag#endElement()
     */
    @Override
	protected int endElement() throws IOException, JspException {
        return EVAL_PAGE;
    }

    /**
     * This method returns the object.
     * 
     * @return    Returns the object.
     */
    public Object getObject() {
        return (this.object);
    }

    /**
     * This method sets the object.
     *
     * @param    aObject    The object to set.
     */
    public void setObject(Object aObject) {
        this.object = aObject;
    }

    /**
     * This method returns the destination.
     * 
     * @return    Returns the destination.
     */
    public String getDestination() {
        return (this.destination);
    }

    /**
     * This method sets the destination.
     *
     * @param    aDestination    The destination to set.
     */
    public void setDestination(String aDestination) {
        this.destination = aDestination;
    }

    /**
     * This method returns the hideImage.
     * 
     * @return    Returns the hideImage.
     */
    public boolean isHideImage() {
        return (this.hideImage);
    }

    /**
     * This method sets the hideImage.
     *
     * @param    aHideImage    The hideImage to set.
     */
    public void setHideImage(boolean aHideImage) {
        this.hideImage = aHideImage;
    }
}

