/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;

/**
 * Form tag that creates its display through a custom {@link ControlProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomInputTag extends AbstractFormFieldControlTag {

	private ControlProvider controlProvider = DefaultFormFieldControlProvider.INSTANCE;

	/**
	 * Sets the {@link ControlProvider} to use for rendering the {@link #getMember()}.
	 */
	public void setControlProvider(ControlProvider controlProvider) {
		this.controlProvider = controlProvider == null ? DefaultFormFieldControlProvider.INSTANCE : controlProvider;
	}
	
	@Override
	public Control createControl(FormMember member, String displayStyle) {
		return controlProvider.createControl(member, displayStyle);
	}

	@Override
	public Control createControl(Object model) {
		return controlProvider.createControl(model);
	}

	@Override
	public HTMLFragment createFragment(Object model) {
		return controlProvider.createFragment(model);
	}

	@Override
	public HTMLFragment createFragment(Object model, String displayStyle) {
		return controlProvider.createFragment(model, displayStyle);
	}

	@Override
	protected void teardown() {
	    super.teardown();

	    this.controlProvider = DefaultFormFieldControlProvider.INSTANCE;
	}
}
