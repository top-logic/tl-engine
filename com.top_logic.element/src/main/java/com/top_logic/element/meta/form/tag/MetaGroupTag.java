/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.element.meta.gui.CreateAttributedComponent;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.tag.AbstractTag;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;

/**
 * Groups the attribute, label and attributes tag.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MetaGroupTag extends AbstractTag {

    private Wrapper attributedObject;

    private TLClass metaElement;

    private Map derivedME = new HashMap();

    /**
     * Setter for {@link #getAttributedObject()}.
     */
    public void setObject(Wrapper anAttributedObject) {
        this.attributedObject = anAttributedObject;
    }

    /**
     * Setter for {@link #getMetaElement()}.
     */
    public void setObject(TLClass aMetaElement) {
        this.metaElement = aMetaElement;
    }

    /**
     * The object whose attributes are displayed in this group.
     */
    public Wrapper getAttributedObject() {
        return this.attributedObject;
    }

    /**
     * The type whose attributes are displayed in this group.
     */
    public TLClass getMetaElement() {
        return this.metaElement;
    }

    /**
     * @see com.top_logic.layout.form.tag.AbstractTag#teardown()
     */
    @Override
	protected void teardown() {
        super.teardown();

        this.attributedObject = null;
        this.metaElement      = null;
    }

    /**
     * @see com.top_logic.layout.form.tag.AbstractTag#endElement()
     */
    @Override
	protected int endElement() throws IOException, JspException {
        return TagSupport.EVAL_PAGE;
    }

    /**
     * @see com.top_logic.layout.form.tag.AbstractTag#startElement()
     */
    @Override
	protected int startElement() throws JspException, IOException {
        if ((this.attributedObject == null) && (this.metaElement == null)) {
            Object          theModel;
            LayoutComponent theComponent = this.getComponent();

            if (theComponent instanceof CreateAttributedComponent) {
                theModel = ((CreateAttributedComponent) theComponent).getMetaElement();
            }
            else {
                theModel = theComponent.getModel();
            }

			if (theModel instanceof TLClass) {
				this.metaElement = (TLClass) theModel;
			} else if (theModel instanceof Wrapper) {
                this.attributedObject = (Wrapper) theModel;
			} else {
                return TagSupport.SKIP_BODY;
            }
        }

        return TagSupport.EVAL_BODY_INCLUDE;
    }

    /**
     * The derived type registered for the given path.
     *
     * @see #addDerivedME(String, TLClass)
     */
    protected TLClass getDerivedME(String aPath) {
        return (TLClass) this.derivedME.get(aPath);
    }

    /**
     * Registers the derived type for the given path.
     *
     * @see #getDerivedME(String)
     */
    protected void addDerivedME(String aPath, TLClass aDerivedME) {
        this.derivedME.put(aPath, aDerivedME);
    }

}

