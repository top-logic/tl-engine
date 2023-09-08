/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.basic.NamedConstant;
import com.top_logic.layout.Control;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.tag.util.BooleanAttribute;

/**
 * JSP tag that renders a {@link SelectionControl}.
 * 
 * @author    <a href=mailto:cdo@top-logic.com>Christian Domsch</a>
 */
public class PopupSelectTag extends AbstractFormFieldControlTag {

	/**
	 * Name of the field that opened the popup.
	 * 
	 * <p>
	 * A value entered in the popup is written back to the control identified by
	 * this parameter.
	 * </p>
	 */
    public static final String PARAM_TARGET_CONTROL = "control";

    /**
	 * Name of the server-side field from which the opened popup loads its
	 * options.
	 * 
	 * @see #PARAM_TARGET_CONTROL
	 */
    public static final String PARAM_TARGET_FIELD = "field";
    
    /**
	 * Name of the form, from which the popup was opened.
	 * 
	 * @see #PARAM_TARGET_CONTROL
	 */
    public static final String PARAM_TARGET_FORM = "form";

    /**
	 * Name of the form's component, which opended to popup.
	 * 
	 * @see #PARAM_TARGET_CONTROL
	 */
    public static final String PARAM_TARGET_COMPONENT = "component";

    public static final Object POPUP_CONFIG_KEY = new NamedConstant("POPUP_CONFIG_KEY");

	/**
	 * Value for {@link #setColumns(int)} that prevents a size attribute to be written.
	 */
	public static final int NO_COLUMNS = 0;

    public final BooleanAttribute autoCompletion  = new BooleanAttribute(true);
    public final BooleanAttribute inputField      = new BooleanAttribute(true);
    public final BooleanAttribute clearButton     = new BooleanAttribute(true);

	private int columns = NO_COLUMNS;

	private Renderer<Object> optionRenderer;
    
    // Setters
    
    public void setClearButton(String hasClearButton) {
        this.clearButton.set(hasClearButton);
    }
    
    public void setInputField(String hasInputField) {
    	this.inputField.set(hasInputField);
    }

    public void setAutoCompletion(String useAutoCompletion) {
    	this.autoCompletion.set(useAutoCompletion);
    }
    
	/**
	 * Number of text columns for the input field.
	 * 
	 * @see #NO_COLUMNS
	 */
	public void setColumns(int value) {
		this.columns = value;
    }

	public void setRenderer(Renderer<?> anOptionRenderer) {
		this.optionRenderer = anOptionRenderer.generic();
    }

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		SelectField selectField = (SelectField) member;
		SelectionControl selectionControl = new SelectionControl(selectField);
		// Create one.
		if (columns > NO_COLUMNS) {
			selectionControl.setColumns(columns);
		}
		if (optionRenderer != null) {
			selectionControl.setOptionRenderer(optionRenderer);
		}
		if (clearButton.isSet()) {
			selectionControl.setClearButton(clearButton.get());
		}
		if (inputField.isSet()) {
			selectionControl.setInputField(inputField.get());
		}
		if (autoCompletion.isSet()) {
			selectionControl.setUseAutoCompletion(autoCompletion.get());
		}
		if (this.style.isSet()) {
			selectionControl.setInputStyle(this.style.getAsString());
		}

		return selectionControl;
	}

    /** 
     * @see com.top_logic.layout.form.tag.AbstractTag#teardown()
     */
    @Override
	protected void teardown() {
    	clearButton.reset();
		columns = NO_COLUMNS;
    	optionRenderer = null;
    	inputField.reset();
    	autoCompletion.reset();
    	super.teardown();
    }

}
