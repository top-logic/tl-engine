/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ExpandableTextInputControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.tag.util.StringAttribute;
import com.top_logic.util.Utils;


/**
 * View of a {@link FormField} rendered as plain text input field.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextInputTag extends AbstractFormFieldControlTag {

	/**
	 * Value for {@link #setColumns(int)} that prevents a size attribute to be written.
	 */
	public static final int NO_COLUMNS = 0;

	protected Boolean multiLine;
	protected boolean expandable = false;

	protected int columns = NO_COLUMNS;
	protected int maxLengthShown = -1;
	protected int rows;
	protected boolean hasRows;
	protected String onFocus;
	protected String onInput;
	protected String type;
    public final StringAttribute immutableStyle = new StringAttribute();

	/**
	 * @see TextInputControl#setMultiLine(boolean)
	 */
	public void setMultiLine(boolean multiLine) {
		this.multiLine = Boolean.valueOf(multiLine);
	}

	/**
	 * Number of text columns to use for the input field.
	 * 
	 * @see TextInputControl#setColumns(int)
	 * @see #NO_COLUMNS
	 */
	public void setColumns(int columns) {
		this.columns = columns;
	}
	
	/**
	 * @see TextInputControl#setRows(int)
	 */
	public void setRows(int rows) {
		this.rows = rows;
		this.hasRows = true;
	}
	
	public void setMaxLengthShown(int aMaxLengthShown) {
        this.maxLengthShown = aMaxLengthShown;
    }

    public void setExpandable(boolean isExpandable) {
        this.expandable = isExpandable;
    }
	
	/**
	 * @see TextInputControl#setOnFocus(com.top_logic.layout.DynamicText)
	 */
	public void setOnfocus(String onFocus) {
		this.onFocus = onFocus;
	}
	
	/**
	 * @see TextInputControl#setOnInput(com.top_logic.layout.DynamicText)
	 */
	public void setOnInput(String onInput) {
		this.onInput = onInput;
	}
	
	public void setType(String aType) {
	    this.type = aType;
	}

    public void setImmutableStyle(String aImmutableStyle) {
        this.immutableStyle.set(aImmutableStyle);
    }

	@Override
	public Control createControl(FormMember formField, String displayStyle) {
		
	    TextInputControl control = null;
	    
	    if (expandable) {
	        control = new ExpandableTextInputControl((FormField) formField,maxLengthShown);
	    }
	    else {
	        control = new TextInputControl((FormField) formField);
	    }
	    if(maxLengthShown > -1) {
	    	control.setMaxLengthShown(maxLengthShown);
	    }
		
		// Init control with configuration given in tag attributes. 
		control.setMultiLine(Utils.isTrue(multiLine));
		if (columns > NO_COLUMNS) {
			control.setColumns(columns);
			if (expandable) {
			    ((ExpandableTextInputControl) control).setSingleLineColumns(columns);
			}
		}
		if (hasRows)
			control.setRows(rows);
        if (style.isSet())
            control.setInputStyle(style.get());
        if (immutableStyle.isSet())
            control.setStyle(immutableStyle.get());
        if (!StringServices.isEmpty(onFocus)) {
        	control.setOnFocus(new ConstantDisplayValue(onFocus));
        }
		if (!StringServices.isEmpty(onInput)) {
			control.setOnInput(new ConstantDisplayValue(onInput));
		}
		control.setType(this.type);
		if (tabindex.isSet())
			control.setTabIndex(tabindex.get());
		
		return control;
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		this.multiLine = null;
		this.expandable = false;
		this.columns = NO_COLUMNS;
		this.hasRows = false;
		this.onFocus = null;
		this.onInput = null;
		this.type = null;
		this.immutableStyle.reset();
		this.maxLengthShown = -1;
	}

}
