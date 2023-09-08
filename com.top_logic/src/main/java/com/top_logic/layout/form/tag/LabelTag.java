/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.IdentifierSource;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMember.IDUsage;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.model.SelectField;

/**
 * View a {@link FormMember}'s label.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LabelTag extends AbstractFormFieldControlTag {

	private boolean colon;
	
	private boolean hasIndex;
	
	/**
	 * @see #setIndex(int)
	 */
	private int index;
	
	/**
	 * @see #setOption(Object)
	 */
	private Object option;

	/**
	 * Sets the option index of a select option label.
	 * 
	 * @param index
	 *        Index of the option in the {@link SelectField#getOptions()} list
	 *        that is represented by this label.
	 */
	public void setIndex(int index) {
		if (option != null) throw SelectOptionTag.illegalState();
		this.index = index;
		this.hasIndex = true;
	}
	
	/**
	 * Sets the option of this select option label.
	 * 
	 * @param option
	 *        Option from the {@link SelectField#getOptions()} list that is
	 *        represented by this label.
	 */
	public void setOption(Object option) {
		if (hasIndex) throw SelectOptionTag.illegalState();
		this.option = option;
	}

	/** 
	 * Set if colon should be added
	 * 
	 * @param aColon if true add a colon to the label text
	 */
	public void setColon(boolean aColon) {
		this.colon = aColon;
	}
	
	@Override
	public Control createControl(FormMember member, String displayStyle) {
		LabelControl result = new LabelControl(member);
		
		if (hasIndex) result.setIndex(index);
		if (option != null) result.setOption(option);
		result.setColon(colon);
		if (style.isSet()) result.setStyle(style.get());
		
		return result;
	}
	
	/**
	 * Creates an ID for the input element.
	 *
	 * @param member
	 *        The {@link FormField} model.
	 * @param scope
	 *        The {@link IdentifierSource} to take new identifiers from.
	 * @param usage
	 *        A classification of the usage of the created identifier, see
	 *        {@link FormField#uiIdentifier(IdentifierSource, IDUsage)}.
	 * @param hasIndex
	 *        Whether the <code>index</code> parameter is given. In that case, the methode has the
	 *        same effect as if the <code>option</code> argument was given.
	 * @param index
	 *        The option index in the given {@link SelectField}.
	 * @param option
	 *        An option of a given {@link SelectField}, or <code>null</code>. If
	 *        non-<code>null</code>, an identifier is created for a checkbox for that option.
	 * @return The identifier for the input element.
	 */
	public static String inputIdFor(FormMember member, IdentifierSource scope, IDUsage usage, boolean hasIndex,
			int index, Object option) {
		if (hasIndex) {
			SelectField field = (SelectField) member;
			option = field.getOptions().get(index);
		}

		if (option != null) {
			SelectField field = (SelectField) member;
			return field.uiIdentifier(scope, usage) + "-" + field.getOptionID(option);
		} else {
			return member.uiIdentifier(scope, usage);
		}
	}

	/** 
	 * @see com.top_logic.layout.form.tag.AbstractTag#teardown()
	 */
	@Override
	protected void teardown() {
		super.teardown();
		
		this.colon = false;
		this.hasIndex = false;
		this.option = null;
	} 

}
