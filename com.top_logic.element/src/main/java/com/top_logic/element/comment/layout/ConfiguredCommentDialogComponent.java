/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.comment.layout;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ConfiguredCommentDialogComponent extends CommentDialogComponent {

    public interface Config extends CommentDialogComponent.Config {
		@Name("commentAttributeName")
		@Mandatory
		String getCommentAttributeName();
	}

	private String commentAttributeName;

    /** 
     * Creates a {@link ConfiguredCommentDialogComponent}.
     */
    public ConfiguredCommentDialogComponent(InstantiationContext context, Config someAtts) throws ConfigurationException {
        super(context, someAtts);
        this.commentAttributeName = someAtts.getCommentAttributeName();
    }
    
    /**
     * @see com.top_logic.element.comment.layout.CommentDialogComponent#getCommentsAttributeName()
     */
    @Override
	protected String getCommentsAttributeName() {
        return this.commentAttributeName;
    }

}

