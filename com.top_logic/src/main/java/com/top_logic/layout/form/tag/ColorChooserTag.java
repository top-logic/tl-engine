/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ColorChooserControl;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.form.model.ComplexField;

/**
* View of a {@link ComplexField} with a {@link ColorFormat} format rendered as a
* button.
* 
* @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
*/
public class ColorChooserTag extends AbstractFormFieldControlTag {
	
	@Override
	public Control createControl(FormMember member, String displayStyle) {
		ColorChooserControl result = new ColorChooserControl((ComplexField) member);		
		return result;
	}
	
}
