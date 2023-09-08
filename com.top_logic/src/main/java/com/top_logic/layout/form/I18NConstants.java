/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Collection of resource key constants for the form package.
 * 
 * The names of the constants are structured according to the following pattern:
 * 
 * &lt;DESCRIPTION_OF_THE_MESSAGE> "_" ("SINGULAR" | "PLURAL")? ( "__"
 * &lt;ARGUMENT> ("_" &lt;ARGUMENT>)* )?
 * 
 * For a concrete example, see {@link #FORMAT_INVALID__VALUE_EXAMPLE}.
 * 
 * <b>Note:</b> These "constants" cannot be declared <code>final</code>,
 * because their values are reflectively initialized. For details, see
 * {@link I18NConstantsBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {
	
	public static ResKey1 AMBIGUOUS_INPUT__VALUE;

	public static ResKey1 NO_MATCH_FOR_INPUT__VALUE;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey TRUE_LABEL;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey FALSE_LABEL;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey NONE_LABEL;
	
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey BLOCKED_VALUE_TEXT;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey BLOCKED_SELECTION_TEXT;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey BLOCKED_LIST_TEXT;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey BLOCKED_CHECKBOX_TEXT;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey NO_OPTION_FOUND;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey ERROR_ICON_ALT;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 INTERNAL_ILLEGAL_UPDATE__FIELD_VALUE;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey MANDATORY_EMPTY_SINGLE_SELECTION_LABEL;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey MANDATORY_EMPTY_SINGLE_SELECTION_LABEL_WITH_NO_OPTIONS;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey EMPTY_SINGLE_SELECTION_LABEL;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey EMPTY_SINGLE_SELECTION_LABEL_WITH_NO_OPTIONS;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey EMPTY_SINGLE_SELECTION_LABEL_IMMUTABLE;
    
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 MORE_INPUT_EXPECTED__VALUE;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 MORE_INPUT_EXPECTED__VALUE_EXAMPLE;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 START_WITH_UPPERCASE_LETTER__TARGET;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 NOT_EMPTY__TARGET;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 SELECTION_TO_BIG__MAXSIZE;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey SINGLE_SELECTION_EXPECTED;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 SELECTION_TO_SMALL__MINSIZE;

	/**
	 * I18N resource key pointing to a message pattern that explains that a
	 * value was entered with an illegal format. The message pattern takes to
	 * arguments: the entered value and an example for a valid value.
	 */
	public static ResKey2 FORMAT_INVALID__VALUE_EXAMPLE; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey3 FORMAT_INVALID__VALUE_POSITION_EXAMPLE;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey2 FORMAT_INVALID__VALUE_POSITION;
	
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 LIST_ELEMENT_INVALID__MESSAGE_INDEX;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 OPEN_CALENDAR__LABEL;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 OPEN_CHOOSER__LABEL;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 CLEAR_CHOOSER__LABEL;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 CLEAR_CHOSEN__LABEL;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 CLEAR_FIXED__LABEL;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 FORMAT_INVALID__VALUE; 

	/** Message indicating that a field must not be empty. */
	public static ResKey NOT_EMPTY; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey INVALID_IDENTIFIERS_SINGULAR; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey INVALID_IDENTIFIERS_PLURAL; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey INVALID_OPTION; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 TO_MUCH_EMAIL_ADDRESSES__MAXIMUM_VALUE; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 TO_LESS_EMAIL_ADDRESSES__MINIMUM_VALUE; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey INVALID_EMAIL_PLURAL; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey INVALID_EMAIL_SINGULAR; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey INVALID_EMAIL_ADDRESSES; 

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey INVALID_CURRENCY; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey1 INVALID_TOKEN__AT; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 VALUE_TO_BIG__VALUE_MAXIMUM; 

	public static ResKey2 VALUE_TO_BIG__VALUE_BOUND;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 VALUE_TO_SMALL__VALUE_MINIMUM; 

	public static ResKey2 VALUE_TO_SMALL__VALUE_BOUND;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey2 DATE_TO_BIG__VALUE_MAXIMUM; 

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey2 DATE_TO_SMALL__VALUE_MINIMUM; 

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey2 DATE_STRING_TO_BIG__VALUE_MAXIMUM; 

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey2 DATE_STRING_TO_SMALL__VALUE_MINIMUM; 

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 VALUE_TO_BIG_NOT_EQUALS__VALUE_BOUND; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 VALUE_TO_SMALL_NOT_EQUALS__VALUE_BOUND; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 VALUE_MIGHT_NOT_BE_EQUAL_TO__VALUE_TEST; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey VALUE_EXPECTED; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 INVALID_EXPRESSION__VALUE_PATTERN; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey3 STRING_TO_SHORT__MINIMUM_LENGTH_UNDERFLOW; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 COLLECTION_TO_SMALL__MINIMUM_LENGTH; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey3 EXTENDED_STRING_TOO_LONG__ROWS_COLUMNS_ACTUAL;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey3 STRING_TO_LONG__MAXIMUM_LENGTH_OVERFLOW; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 COLLECTION_TO_LARGE__MAXIMUM_LENGTH; 
	
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey NO_EMPTY_SELECTION; 

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey INVALID_VALUE;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey INVALID_INT_VALUE;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey INVALID_LONG_VALUE;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey INVALID_NUMBER_VALUE;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey INVALID_POSITIVE_NUMBER;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey EDIT_TOKEN_TIMED_OUT; 

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey LOSE_CHANGES_CONFIRM; 
    
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey INVALID_MAIL_ADDRESS;
    
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey1 INPUT_DID_NOT_MATCH_PATTERN__PATTERN;
    
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey1 MIN_ONE_FIELD_NOT_EMPTY__NAMES;
    
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey1 MAX_ONE_FIELD_NOT_EMPTY__NAMES;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey POPUP_SELECT_SUBMIT;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey POPUP_SELECT_SUBMIT_NO_CHANGE;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey POPUP_SELECT_CANCEL;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey1 POPUP_SELECT_TITLE__FIELD;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey1 POPUP_SELECT_FILTER__FIELD;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey1 POPUP_SELECT_OPTIONS__FIELD;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey1 POPUP_SELECT_SELECTED__FIELD;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey3 SEQUENCE_ERROR_GE_AND_LE__FIELD_FIELDSGE_FIELDSLE;
	
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 SEQUENCE_ERROR_GE__FIELD_FIELDSGE;
	
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey2 SEQUENCE_ERROR_LE__FIELD_FIELDSLE;


    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey5 MULTI_SEQUENCE_ERROR_GE_AND_LE__FIELD_FIELDSGE_FIELDGEMAX_FIELDSLE_FIELDLEMIN;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey3 MULTI_SEQUENCE_ERROR_GE__FIELD_FIELDSGE_FIELDGEMAX;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey3 MULTI_SEQUENCE_ERROR_LE__FIELD_FIELDSLE_FIELDLEMIN;

	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey VALUE_NOT_UNIQUE;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey NOT_ONLY_DIGITS;

    /** @see #FORMAT_INVALID__VALUE_EXAMPLE */
    public static ResKey INVALID_CHARACTERS;

	public static ResKey NO_EMPTY_SELECTION_MESSAGE;

	public static ResKey LOSE_CHANGES_CONFIRM_MESSAGE;
	
	public static ResKey ALL_OR_NOTHING_MESSAGE;

	//ColorChooser
	/** @see #FORMAT_INVALID__VALUE_EXAMPLE */
	public static ResKey CHOSEN_COLOR;
	public static ResKey CURRENT_COLOR;

	public static ResKey COLOR_PALETTE;

	public static ResKey COLOR_MIXER;

	public static ResKey RED_COMPONENT;
	public static ResKey GREEN_COMPONENT;
	public static ResKey BLUE_COMPONENT;
	public static ResKey HEX_CODE;

	public static ResKey CUSTOMIZE_COLOR_TOOLTIP;

	public static ResKey COLOR_CHOOSER_TITLE;

	public static ResKey RESET_COLORS;

	public static ResKey ONLY_FIXED_OPTIONS_SELETED_MESSAGE;

	public static ResKey SELECT_ARROW_LEFT;
	public static ResKey SELECT_ARROW_DOUBLE_LEFT;
	public static ResKey SELECT_ARROW_RIGHT;
	public static ResKey SELECT_ARROW_DOUBLE_RIGHT;

	static {
		initConstants(I18NConstants.class);
	}

}
