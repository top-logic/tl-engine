/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.comment.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.element.comment.wrap.Comment;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class CommentTableModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link CommentTableModelBuilder} instance.
	 */
	public static final CommentTableModelBuilder INSTANCE = new CommentTableModelBuilder();

	protected CommentTableModelBuilder() {
		// Singleton constructor.
	}

        /**
         * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
         */
        @Override
		public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
            CommentTableComponent theComp  = (CommentTableComponent) aComponent;
		Wrapper theModel = (Wrapper) businessModel;

            if (theModel != null) {
                Collection theValues = (Collection) theModel.getValue(theComp.getCommentsAttributeName());

                if (theValues != null) {
                    return new ArrayList(theValues);
                }
            } 

            return Collections.EMPTY_LIST;
        }

        /**
         * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
         */
        @Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        	return (aComponent instanceof CommentTableComponent) && (aModel instanceof Wrapper);
        }

        /**
         * @see com.top_logic.mig.html.ListModelBuilder#retrieveModelFromListElement(com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
         */
        @Override
		public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
//            Comment theComment = (Comment) anObject;
//
            return null;
        }

        /**
         * @see com.top_logic.mig.html.ListModelBuilder#supportsListElement(com.top_logic.mig.html.layout.LayoutComponent, java.lang.Object)
         */
        @Override
		public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
            return (anObject instanceof Comment);
        }
    }
