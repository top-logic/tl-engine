/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form;

import com.top_logic.layout.form.boxes.reactive_tag.GroupCellControl;

/**
 * Collection of CSS classes used in reactive forms.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReactiveFormCSS {

	/** Content is organised in columns. */
	String RF_COLUMNS_LAYOUT = "rf_columnsLayout";

	/** Container for forms. */
	String RF_CONTAINER = "rf_container";

	/** CSS class to indicate that the form must not have a line break. */
	String CSS_CLASS_KEEP = "keep";

	/** CSS class for form cells. */
	String RF_CELL = "rf_cell";

	/** CSS class for form labels. */
	String RF_LABEL = "rf_label";

	/**
	 * CSS class to mark an element to be rendered over the whole display width. The number of
	 * columns doesn't matter.
	 */
	String RF_LINE = "rf_line";

	/** CSS class to render an cell in a reactive form. */
	String RF_INPUT_CELL = "rf_inputCell";

	/** CSS class for an empty cell in a reactive form. */
	String RF_EMPTY_CELL = "rf_emptyCell";

	/**
	 * CSS class to mark that the label should be displayed on the same line as the value.
	 * 
	 * @see #RF_LABEL_ABOVE
	 */
	String RF_LABEL_INLINE = "rf_labelInline";

	/**
	 * CSS class to mark that the label should be displayed above the value.
	 * 
	 * @see #RF_LABEL_INLINE
	 */
	String RF_LABEL_ABOVE = "rf_labelAbove";

	/**
	 * CSS class to mark a {@link #RF_INPUT_CELL} that it contains no label.
	 */
	String RF_NO_LABEL = "rf_noLabel";

	/**
	 * CSS class to mark a {@link #RF_INPUT_CELL} that it consists of a label and content area.
	 */
	String RF_WITH_LABEL = "rf_withLabel";

	/**
	 * CSS class for the element of a {@link GroupCellControl} that contains the menu.
	 * 
	 * @see GroupCellControl#hasMenu()
	 */
	String RF_MENU = "rf_menu";

	public String CELL_SMALL_CSS = "rf_cellSmall";
}

