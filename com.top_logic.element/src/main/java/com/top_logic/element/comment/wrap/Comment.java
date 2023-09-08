/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.comment.wrap;

import java.util.Collection;
import java.util.Date;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * Holds a single comment.
 *
 * @author    <a href="mailto:tsa@top-logic.de">Theo Sattler</a>
 *
 */
public class Comment extends AttributedWrapper {

    public static final String CONTENT_ATTRIBUTE = "content";
    public static final String KO_TYPE = "Comment";

    public Comment(KnowledgeObject ko) {
        super(ko);
    }

    /**
     * @see AbstractWrapper#compareTo(Wrapper)
     */
    @Override
	public int compareTo(Wrapper anOtherWrapper) {
		{
            if ( ! (anOtherWrapper instanceof Comment)) {
                return super.compareTo(anOtherWrapper);
            } else {
                Comment theOther = (Comment) anOtherWrapper;
                Date theOtherDate = theOther.getCreated();
                Date theDate      = this.getCreated();
                int theResult = theDate.compareTo(theOtherDate);
                if (theResult != 0) {
                    return theResult;
                }
                Person theOtherCreator = theOther.getCreator();
                Person theCreator      = this.getCreator();
                theResult = theCreator.compareTo(theOtherCreator);
                if (theResult != 0) {
                    return theResult;
                }
                return super.compareTo(anOtherWrapper);
            }
        }
    }

    public String getContent() {
        return this.getValue(CONTENT_ATTRIBUTE).toString();
    }

    public void setContent(String aNewContent) {
        this.setValue(CONTENT_ATTRIBUTE, aNewContent);
    }


    /**
     * Gets the context object of this comment (the object this comment is assigned to).
     */
    public Wrapper getContext() {
		Collection<Wrapper> potentialContexts = WrapperMetaAttributeUtil.getWrappersWithValue(this);
		if (potentialContexts.size() > 1) {
			Logger.warn("Comment is attached to multiple context objects. Will use first one.", Comment.class);
        }
		return CollectionUtil.getFirst(potentialContexts);
    }

    @Override
    public BoundObject getSecurityParent() {
        Wrapper context = getContext();
        return context instanceof BoundObject ? (BoundObject)context : super.getSecurityParent();
    }



    /**
     * Create a new comment object
     *
     * @param aContent  the content of the comment
     *
     * @return the new comment wrapper, null if an exception occurs
     */
    public static Comment createComment(String aContent) {
        try {
            KnowledgeBase   theKB = getDefaultKnowledgeBase();
			KnowledgeObject theKO = theKB.createKnowledgeObject(KO_TYPE);
            theKO.setAttributeValue(CONTENT_ATTRIBUTE, aContent);
			return (Comment) WrapperFactory.getWrapper(theKO);
        } catch (Exception e) {
            Logger.error("Could not createComment()", e, Comment.class);
            return null;
        }
    }

}
