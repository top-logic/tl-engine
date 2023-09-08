/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.tag.SelectionTableTag;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The GroupedMetaTableTag representds a table tag in a meta group
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GroupedMetaTableTag extends SelectionTableTag {

    /**
     * @see com.top_logic.layout.form.tag.AbstractFormMemberTag#setName(java.lang.String)
     */
    @Override
	public void setName(String aName) {
		super.setName(GroupedMetaTableTag.getRealTagName(this, pageContext, aName));
    }

	public static String getRealTagName(Tag aTag, PageContext pageContext, String aName) {
        try {
            MetaGroupTag ancestorGroupTag = (MetaGroupTag) TagSupport.findAncestorWithClass(aTag, MetaGroupTag.class);
            
            if (ancestorGroupTag == null) {
                throw new RuntimeException("Table tag '" + aName + "' is not located within an meta group tag (meta:group)!");
            }

            Wrapper      theAttributed = ancestorGroupTag.getAttributedObject();
			TLClass theME =
				(TLClass) ((theAttributed == null) ? ancestorGroupTag.getMetaElement() : theAttributed.tType());
			TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(theME, aName);

			MetaTagUtil.addDisplayedAttribute(pageContext, theAttributed, theMA);
            
            return (MetaAttributeGUIHelper.getAttributeID(theMA, theAttributed));
        }
        catch (NoSuchAttributeException ex) {
            throw new RuntimeException("Unable to get attribute update for '" + aName + "'!", ex);
        }
    }
}

