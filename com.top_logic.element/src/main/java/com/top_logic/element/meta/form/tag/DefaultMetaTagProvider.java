/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.top_logic.basic.Logger;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.tag.FormTag;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The {@link DefaultMetaTagProvider} is the default implementation of
 * {@link MetaTagProvider} which produces the tags and sets the
 * {@link TLStructuredTypePart}, the {@link Wrapper} and the parent.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultMetaTagProvider implements MetaTagProvider {

	public static final MetaTagProvider	INSTANCE	= new DefaultMetaTagProvider();

	@Override
	public Tag createInputTag(String aMetaAttributeName, Wrapper anAttributed, Tag aParentTag) {
		return initTag(new MetaInputTag(), aParentTag, aMetaAttributeName, anAttributed);
	}

	@Override
	public Tag createLabelTag(String aMetaAttributeName, Wrapper anAttributed, Tag aParentTag) {
		return initTag(new MetaLabelTag(), aParentTag, aMetaAttributeName, anAttributed);
	}

	@Override
	public Tag createErrorTag(String aMetaAttributeName, Wrapper anAttributed, Tag aParentTag) {
		return initTag(new MetaErrorTag(), aParentTag, aMetaAttributeName, anAttributed);
	}

	/**
	 * This method initializes <code>aTag</code>. If there is no
	 * {@link FormField} for the {@link TLStructuredTypePart} provided by
	 * <code>aMetaAttributeName</code> or the {@link TLStructuredTypePart} does not
	 * exists, <code>null</code> will be returned.
	 */
	private Tag initTag(AbstractMetaTag aTag, Tag aParentTag, String aMetaAttributeName, Wrapper anAttributed) {
		// gets the attribute
		TLStructuredTypePart metaAttribute = (TLStructuredTypePart) anAttributed.tType().getPart(aMetaAttributeName);
		if (metaAttribute == null) {
			Logger.error("Unable to get attribute '" + aMetaAttributeName + "'", this);
			return null;
		}

		// checks if there is a FormMember for the attribute
		FormContext formContext = getFormContext(aParentTag);
		String formFieldName = MetaAttributeGUIHelper.getAttributeID(metaAttribute, anAttributed);
		boolean hasMember = formContext.hasMember(formFieldName);

		if (hasMember) {
			aTag.setMetaAttribute(metaAttribute);
			aTag.setAttributed(anAttributed);
			aTag.setParent(aParentTag);
			return aTag;
		} else {
			Logger.warn("Attribute '" + aMetaAttributeName
				+ "' could not be written, since there is no corresponding form member.", this);
			return null;
		}
	}

	/**
	 * This method returns the {@link FormContext} of <code>aTag</code>.
	 */
	public static FormContext getFormContext(Tag aTag) {
		FormTag theFormTag = (FormTag) TagSupport.findAncestorWithClass(aTag, FormTag.class);
		FormContext formContext = theFormTag.getFormContext();
		return formContext;
	}
}
