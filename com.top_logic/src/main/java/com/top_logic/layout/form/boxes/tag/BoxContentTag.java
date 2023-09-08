/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import com.top_logic.layout.form.tag.ControlBodyTag;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link BoxTag} that may have direct textual contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BoxContentTag extends BoxTag, ControlBodyTag {

	/**
	 * The minimum number of columns to reserve.
	 */
	void setColumns(int columns);

	/**
	 * The minimum number of rows to reserve.
	 */
	void setRows(int rows);

	/**
	 * The CSS class of the generated {@link HTMLConstants#TD} tag.
	 */
	void setCssClass(String cssClass);

	/**
	 * The inline style of the generated {@link HTMLConstants#TD} tag.
	 */
	void setStyle(String style);

	/**
	 * Width style value of the generated {@link HTMLConstants#TD} tag.
	 */
	void setWidth(String widthSpec);

}
