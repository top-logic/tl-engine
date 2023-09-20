/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.comment.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.comment.wrap.Comment;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.wrap.person.CommonPersonLabelProvider;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.util.TLModelUtil;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CommentResourceProvider extends WrapperResourceProvider {

    public static final CommentResourceProvider INSTANCE_COMMENT = new CommentResourceProvider();

    @Override
	public String getLabel(Object anObject) {
		Person creator = ((Comment) anObject).getCreator();
		return CommonPersonLabelProvider.getLabelNameTitleFirst(creator, false);
    }
    
    @Override
	protected ResKey getTooltipNonNull(Object object) {
		return I18NConstants.COMMENT_TOOLTIP.fill(
			quote(getLabel(object)),
			quote(HTMLFormatter.getInstance().formatDate(((Comment) object).getCreated())),
			quote(MetaResourceProvider.INSTANCE.getLabel(TLModelUtil.type(object))));
    }
    
}
