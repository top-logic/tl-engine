/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.BeaconControl;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.SelectField;

/**
 * View of a {@link SelectField} (with single selection) rendered as a series of
 * icons.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BeaconTag extends AbstractFormFieldControlTag {
    
    /** The type of icons to display (e.g. "greenYellowRed", "greenRed" ...). */
    protected String type;

    /**
	 * JSP tag setter for {@link #type}.
     */
    public void setType(String aType) {
        type = aType;
    }
    
	@Override
	public Control createControl(FormMember member, String displayStyle) {
		BeaconControl result;
		
		if (member instanceof SelectField) {
			result = new BeaconControl((SelectField) member);
		} else {
			result = new BeaconControl((ComplexField) member);
		}
		
		result.setType(type);
		if (style.isSet()) {
			result.setStyle(style.get());
		}
		return result;
	}

}
