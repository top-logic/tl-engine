/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Collection of resource key constants for the form package.
 *
 * <b>Note:</b> These "constants" cannot be declared <code>final</code>, because their
 * values are reflectively initialized. For details, see {@link I18NConstantsBase}.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/** Key when table is disabled */
	public static ResKey TABLE_DISABLED;

	/** Key when no row is currently selected */
	public static ResKey NO_ROW_SELECTED;

	/** Key for the add row command in the TableListControl and EditableTableControl */
    public static ResKey ADD_ROW;
	public static ResKey ADD_ROW_DISABLED;

    /** Key for the move row up command in the TableListControl and EditableTableControl */
    public static ResKey MOVE_ROW_UP;
    public static ResKey MOVE_ROW_UP_DISABLED;

    /** Key for the move row down command in the TableListControl and EditableTableControl */
    public static ResKey MOVE_ROW_DOWN;
    public static ResKey MOVE_ROW_DOWN_DISABLED;

    /** Key for the move row to top command in the TableListControl and EditableTableControl */
    public static ResKey MOVE_ROW_TO_TOP;
	public static ResKey MOVE_ROW_TO_TOP_DISABLED;

    /** Key for the move row to bottom command in the TableListControl and EditableTableControl */
    public static ResKey MOVE_ROW_TO_BOTTOM;

	public static ResKey MOVE_ROW_TO_BOTTOM_DISABLED;

    /** Key for the remove row command in the TableListControl and EditableTableControl */
    public static ResKey REMOVE_ROW;
    public static ResKey REMOVE_ROW_DISABLED;
    
    /** Key for the open chooser command in EditableTableControl */
    public static ResKey OPEN_CHOOSER;

    /**
     * Cancel button label for multi-selection filter menu.
     */
    public static ResKey FILTER_CANCEL;
    
    /**
     * Apply button label for multi-selection filter menu.
     */
    public static ResKey FILTER_APPLY;
    
    /**
     * Label for buttons, which reset the filter to its initial state
     */
    public static ResKey FILTER_RESET;
    
    /**
     * Label for buttons, which deactivate filtering.
     */
    public static ResKey FILTER_SHOW_ALL;

	/**
	 * Label for field, which indicates inverted filtering.
	 */
	public static ResKey FILTER_INVERT;

	/**
	 * Header text for filter menu.
	 */
    public static ResKey FILTER_HEADER;
    
    /**
     * Special filter for numbers.
     */
    public static ResKey FILTER_NUMBERS;
    
    /**
     * Special filter for non-alpha-numeric characters.
     */
    public static ResKey FILTER_SPECIAL_CHARS;
    
	public static ResKey ERROR_TABLE_FILTERING;

	public static ResKey ERROR_TABLE_SORTING;

	public static ResKey ERROR_TABLE_FILTERING_SORTING;

    static {
        initConstants(I18NConstants.class);
    }

}
