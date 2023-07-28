/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.model.form;

/**
 * Collection of CSS classes used in reactive forms.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReactiveFormCSS {

	/** Content is organised in columns. */
	String RF_COLUMNS_LAYOUT = "rf_columnsLayout";

	/** Marker CSS class to find inner drop target. */
	String RF_INNER_TARGET = "rf_innerTarget";

	String RF_CONTAINER = "rf_container";

	/** CSS class for elements in which an object can be dropped. */
	String RF_DROP_TARGET = "rf_dropTarget";

	String CSS_CLASS_KEEP = "keep";

	String RF_WRAPPER = "rf_wrapper";

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
}

