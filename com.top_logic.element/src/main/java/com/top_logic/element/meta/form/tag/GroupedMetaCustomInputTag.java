/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.layout.form.tag.CustomInputTag;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * The GroupedMetaCustomInputTag adds the functionality of providing custom
 * {@link ControlProvider} to meta tags.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GroupedMetaCustomInputTag extends CustomInputTag {

    @Override
    public void setName(String aName) {
		super.setName(GroupedMetaTableTag.getRealTagName(this, pageContext, aName));
    }
}
