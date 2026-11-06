/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ChoiceControl;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.tag.util.BooleanAttribute;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * View of a {@link SelectField} rendered as set of radiobuttons or checkboxes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChoiceTag extends AbstractFormFieldControlTag {
	
	/** The orientation of the rendered options. */
	public Orientation orientation = null;

    /** Whether the options are indented. */
    public final BooleanAttribute indent = 
        new BooleanAttribute(false);
    
	/** Whether the labels are displayed before the radio buttons or checkboxes. */
	public final BooleanAttribute labelsLeft = 
		new BooleanAttribute(false);

	/**
	 * Sets the orientation of rendered radio buttons.
	 */
	public void setOrientation(Orientation value) {
		orientation = value;
	}
	
    /**
     * @see "Choice-Tag configuration in ajax-form.tld"
     */
    public void setIndent(boolean value) {
        indent.setAsBoolean(value);
    }
    
	/** Sets whether the labels are displayed before the radio buttons or checkboxes. */
	public void setLabelsLeft(boolean aLabelsRight) {
		labelsLeft.setAsBoolean(aLabelsRight);
	}
	
	@Override
	protected void teardown() {
		super.teardown();
        indent.reset();
		orientation = Orientation.VERTICAL;
		labelsLeft.reset();
	}

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		ChoiceControl control = new ChoiceControl((SelectField) member);
		initControl(control);
		if (indent.isSet()) control.setIndent(indent.get());
		if (orientation != null) {
			control.setOrientation(orientation);
		}
		if (labelsLeft.isSet()) control.setLabelsLeft(labelsLeft.get());
		return control;
	}

}
