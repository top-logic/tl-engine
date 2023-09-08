/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Map;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FixedOptionsListener;
import com.top_logic.layout.form.model.OptionsListener;
import com.top_logic.layout.form.model.SelectField;

/**
 * {@link AbstractFormFieldControl}, that deals with {@link SelectField}s.
 * 
 * @author     <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractSelectControl extends AbstractFormFieldControl
		implements OptionsListener, FixedOptionsListener {

	/**
	 * Create a new {@link AbstractSelectControl}.
	 */
	protected AbstractSelectControl(FormField model, Map commandsByName) {
		super(model, commandsByName);
	}

	@Override
	protected void registerListener(FormMember member) {
		super.registerListener(member);
		member.addListener(SelectField.OPTIONS_PROPERTY, this);
		member.addListener(SelectField.FIXED_FILTER_PROPERTY, this);
	}

	@Override
	protected void deregisterListener(FormMember member) {
		member.removeListener(SelectField.OPTIONS_PROPERTY, this);
		member.removeListener(SelectField.FIXED_FILTER_PROPERTY, this);
		super.deregisterListener(member);
	}

	@Override
	public Bubble handleFixedOptionsChanged(SelectField sender, Filter oldValue, Filter newValue) {
		return repaintOnEvent(sender);
	}

	@Override
	public Bubble handleOptionsChanged(SelectField sender) {
		return repaintOnEvent(sender);
	}

	/**
	 * {@link SelectField} handled by this {@link AbstractSelectControl}
	 * 
	 * @see #getFieldModel()
	 */
	public final SelectField getSelectField() {
		return (SelectField) getFieldModel();
	}
}
