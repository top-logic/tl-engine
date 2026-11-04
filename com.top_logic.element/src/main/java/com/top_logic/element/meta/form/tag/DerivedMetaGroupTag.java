/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.io.IOException;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.model.TLClass;
import com.top_logic.layout.form.tag.AbstractTag;

/**
 * Tag registering a derived type for a path at the enclosing {@link MetaGroupTag}.
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DerivedMetaGroupTag extends AbstractTag {

    String path;
    TLClass metaElement;

    /**
     * @see com.top_logic.layout.form.tag.AbstractTag#endElement()
     */
    @Override
	protected int endElement() throws IOException, JspException {
        MetaGroupTag theAncestorGroupTag = (MetaGroupTag) TagSupport.findAncestorWithClass(this, MetaGroupTag.class);
        theAncestorGroupTag.addDerivedME(this.path, this.metaElement);
        return EVAL_PAGE;
    }

    /**
     * @see com.top_logic.layout.form.tag.AbstractTag#startElement()
     */
    @Override
	protected int startElement() throws JspException, IOException {
        return SKIP_BODY;
    }

    /**
     * Setter for {@link #getMetaElement()}.
     */
    public void setObject(TLClass aMetaElement) {
        this.metaElement = aMetaElement;
    }

    /**
     * The derived type to register.
     */
    public TLClass getMetaElement() {
        return this.metaElement;
    }

    /**
     * The path for which the {@link #getMetaElement() derived type} is registered.
     */
    public String getPath() {
        return (this.path);
    }

    /**
     * Setter for {@link #getPath()}.
     */
    public void setPath(String aPath) {
        this.path = aPath;
    }


}

