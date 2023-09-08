/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControl;

/**
 * {@link AbstractConstantControl}, that renders the title of a table filter group.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FilterGroupTitleControl extends AbstractConstantControl {

	private static final String FILTER_GROUP_TITLE_CLASS = "filterGroupTitle";
	private ResKey _title;

	/**
	 * Create a new {@link FilterGroupTitleControl}.
	 */
	public FilterGroupTitleControl(ResKey title) {
		_title = title;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		out.writeText(context.getResources().getString(_title));
		out.writeText(":");
		out.endTag(DIV);
	}

	@Override
	protected String getTypeCssClass() {
		return FILTER_GROUP_TITLE_CLASS;
	}

}
