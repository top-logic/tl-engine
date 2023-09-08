/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.DefaultCollectionBox;
import com.top_logic.layout.form.boxes.model.LineBox;

/**
 * {@link BoxTag} creating an arbitrary {@link Box}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CellTag extends AbstractBoxContainerTag implements BoxContentTag {

	/**
	 * XML name of this tag.
	 */
	public static final String CELL_TAG = "form:cell";

	@Override
	protected DefaultCollectionBox createCollectionBox() {
		return new LineBox();
	}

	@Override
	protected String getTagName() {
		return CELL_TAG;
	}

	@Override
	public void setColumns(int columns) {
		mkContentBox().setInitialColumns(columns);
	}

	@Override
	public void setRows(int rows) {
		mkContentBox().setInitialRows(rows);
	}

	@Override
	public void setCssClass(String cssClass) {
		mkContentBox().setCssClass(cssClass);
	}

	@Override
	public void setStyle(String style) {
		mkContentBox().setStyle(style);
	}

	@Override
	public void setWidth(String widthSpec) {
		mkContentBox().setWidth(DisplayDimension.parseDimension(widthSpec));
	}

}
