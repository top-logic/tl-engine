/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.IntegerInputControl;
import com.top_logic.layout.form.tag.util.StringAttribute;

/**
 * The IntegerInputTag is a JSP tag that renders an {@link IntegerInputControl}.
 * 
 * @author    <a href="mailto:TEH@top-logic.com">Tobias Ehrler</a>
 */
public class IntegerInputTag extends AbstractFormFieldControlTag {
    
    protected String onFocus;
    protected String onInput;
    protected int columns;
    
    public final StringAttribute immutableStyle = new StringAttribute();

    public void setImmutableStyle(String aImmutableStyle) {
        this.immutableStyle.set(aImmutableStyle);
    }

    /**
     * This method sets the onFocus.
     *
     * @param    aOnFocus    The onFocus to set.
     */
    public void setOnFocus(String aOnFocus) {
        this.onFocus = aOnFocus;
    }

    /**
	 * This method sets the oninput.
	 *
	 * @param onInput
	 *        The onKeyUp to set.
	 */
    public void setOnInput(String onInput) {
        this.onInput = onInput;
    }

    /**
     * This method sets the columns.
     *
     * @param    aColumns    The columns to set.
     */
    public void setColumns(int aColumns) {
        this.columns = aColumns;
    }

    @Override
	public Control createControl(FormMember formField, String displayStyle) {
        
        IntegerInputControl control = new IntegerInputControl((FormField) formField);
        
        if (style.isSet())
            control.setInputStyle(style.get());
        if (immutableStyle.isSet())
            control.setStyle(immutableStyle.get());
        if (tabindex.isSet())
            control.setTabIndex(tabindex.get());
        if (!StringServices.isEmpty(onFocus)) {
        	control.setOnFocus(new ConstantDisplayValue(onFocus));
        }
        if (StringServices.isEmpty(onInput)) {
        	control.setOnInput(new ConstantDisplayValue(onInput));
        }
        control.setColumns(columns);
        
        return control;
    }
    
    /**
     * @see com.top_logic.layout.form.tag.AbstractFormFieldControlTag#teardown()
     */
    @Override
	protected void teardown() {
        super.teardown();
        this.immutableStyle.reset();
        this.onFocus = null;
        this.onInput = null;
    }

}

