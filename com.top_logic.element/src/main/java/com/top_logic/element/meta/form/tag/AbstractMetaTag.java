/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.tag.AbstractProxyTag;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;


/**
 * Starting point for the taglib parts of the attributed package.
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractMetaTag extends AbstractProxyTag {

	/** The attribute. */
	private TLStructuredTypePart metaAttributeObj;

	/** The object. May be <code>null</code>. */
	private TLObject attributedObj;

    /** The attribute update. May be null. */
	private AttributeUpdate attributeUpdate;

	private String part;

	private String domain;

    /**
	 * Set the attribute. Must not be <code>null</code>.
	 *
	 * @param aMetaAttribute
	 *        the attribute
	 */
    public void setMetaAttribute(TLStructuredTypePart aMetaAttribute) {
    	if (aMetaAttribute == null) {
			throw new IllegalArgumentException("Attribute must not be null");
    	}

    	this.metaAttributeObj = aMetaAttribute;
    }

    /**
	 * Get the attribute.
	 *
	 * @return the attribute
	 */
    public TLStructuredTypePart getMetaAttribute() {
    	return (this.metaAttributeObj);
    }

	/**
	 * Set the object. Must not be <code>null</code>.
	 *
	 * @param anAttributed
	 *        the object
	 */
	public void setAttributed(Wrapper anAttributed) {
		this.attributedObj = anAttributed;
	}

	/**
	 * Get the object.
	 *
	 * @return the object
	 */
	public TLObject getAttributed() {
		return this.attributedObj;
	}

    public String getPart() {
        return part;
    }

    public void setPart(String aPart) {
        this.part = aPart;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String aDomain) {
        this.domain = aDomain;
    }

	@Override
	protected void teardown() {
    	super.teardown();

        this.metaAttributeObj = null;
        this.attributeUpdate = null;
		this.attributedObj = null;
		this.part = null;
		this.domain = null;
    }

	protected String getFieldName() {
		String baseName =
			MetaAttributeGUIHelper.internalID(this.getMetaAttribute(), this.getAttributed(), this.getDomain());

		if (part != null) {
			return baseName + "." + part;
		} else {
			return baseName;
		}
	}

    /**
     * Get the attribute update, may be null
     */
	public final AttributeUpdate getAttributeUpdate() {
        return (attributeUpdate);
    }

    /**
     * Set the attribute update, sets also the attributed object
     * and the meta attribute accordingly.
     */
    public void setAttributeUpdate(AttributeUpdate anAttributeUpdate) {
        this.attributeUpdate  = anAttributeUpdate;
        this.attributedObj    = anAttributeUpdate.getObject();
        this.metaAttributeObj = anAttributeUpdate.getAttribute();
    }

}
