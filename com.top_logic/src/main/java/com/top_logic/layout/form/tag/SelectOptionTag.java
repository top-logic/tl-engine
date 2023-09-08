/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.SelectOptionControl;
import com.top_logic.layout.form.model.SelectField;

/**
 * Tag for rendering a single select option input of a {@link SelectField}.
 * 
 * <p>
 * The corresponding label can be rendered with the regular {@link LabelTag} by
 * giving an additional index or option argument.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectOptionTag extends AbstractFormFieldControlTag {

	/**
	 * @see #setOption(Object)
	 */
	private Object option;
	
	/**
	 * @see #setIndex(int)
	 */
	private int optionIndex;
	private boolean hasOptionIndex;
	
	/**
	 * The index of the option to render.
	 * 
	 * @see #setOption(Object) For an alternative possibility to specify the
	 *      option.
	 */
	public void setIndex(int value) {
		if (hasOption()) throw illegalState();
		
		this.optionIndex = value;
		this.hasOptionIndex = true;
	}

	private boolean hasOption() {
		return option != null;
	}

	/**
	 * The option to render.
	 * 
	 * @see #setIndex(int) For an alternative possibility to specify the
	 *      option.
	 */
	public void setOption(Object value) {
		if (hasOptionIndex) throw illegalState();
		
		this.option = value;
	}

	public static IllegalStateException illegalState() {
		return new IllegalStateException("Only one property 'option' or 'index' may be present.");
	}
	
	@Override
	public Control createControl(FormMember formField, String displayStyle) {
		SelectField selectField = (SelectField) formField;
		if (! hasOptionIndex && ! hasOption()) 
			throw new IllegalStateException("One of the properties 'index' or 'option' is required.");

		SelectOptionControl control = 
			new SelectOptionControl(selectField, getRepresentedOption(selectField));
		if (style.isSet()) control.setInputStyle(style.get());
		if (tabindex.isSet()) control.setTabIndex(tabindex.get());
		return control;
	}

	private Object getRepresentedOption(SelectField selectField) {
		return hasOptionIndex ? selectField.getOptions().get(optionIndex) : option;
	}

	@Override
	protected void teardown() {
		super.teardown();
		
		this.option = null;
		this.hasOptionIndex = false;
	}
}
