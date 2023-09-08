/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.form.control.BooleanChoiceControl;
import com.top_logic.layout.form.control.CheckboxControl;
import com.top_logic.layout.form.control.DropDownControl;
import com.top_logic.layout.form.control.IconSelectControl;
import com.top_logic.layout.form.control.IconSelectControl.DefaultBooleanTristateResourceProvider;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.model.annotate.ui.BooleanPresentation;

/**
 * View of a {@link BooleanField} rendered as single checkbox.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckboxInputTag extends AbstractFormFieldControlTag {

	private static final BooleanPresentation DEFAULT_DISPLAY = BooleanPresentation.CHECKBOX;

	private boolean _resetable;

	private BooleanPresentation _display = DEFAULT_DISPLAY;
	
	/**
	 * For compatibility only.
	 * 
	 * @see #setDisplay(BooleanPresentation)
	 */
	@Deprecated
	public final void setYesNo(boolean yesNo) {
		setDisplay(BooleanPresentation.RADIO);
	}
	
	public void setDisplay(BooleanPresentation display) {
		_display = display;
	}

	public void setResetable(boolean value) {
		_resetable = value;
	}

	@Override
	public Control createControl(FormMember member, String displayStyle) {
		BooleanField field = (BooleanField) member;
		AbstractFormFieldControl control;
		switch (_display) {
			case CHECKBOX: {
				if (field.isMandatory() || _resetable) {
					IconSelectControl checkbox =
						new IconSelectControl((FormField) member, DefaultBooleanTristateResourceProvider.INSTANCE);
					checkbox.setResetable(_resetable);
					control = checkbox;
				} else {
					control = new CheckboxControl(field);
				}
				break;
			}
			case SELECT: {
				DropDownControl select = new DropDownControl(field, !_resetable);
				control = select;
				break;
			}
			case RADIO: {
				BooleanChoiceControl choice = new BooleanChoiceControl(field);
				choice.setResetable(_resetable);
				control = choice;
				break;
			}
			default: {
				throw new UnreachableAssertion(_display.name());
			}
		}

		if (style.isSet())
			control.setInputStyle(style.get());
		if (tabindex.isSet())
			control.setTabIndex(tabindex.get());
		
		return control;
	}

	@Override
	protected void teardown() {
		_display = DEFAULT_DISPLAY;
		_resetable = false;

		super.teardown();
	}

}
