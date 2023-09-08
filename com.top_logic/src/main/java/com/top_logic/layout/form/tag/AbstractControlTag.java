/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;

/**
 * Similar purpose as {@link AbstractFormMemberControlTag} for form tags.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractControlTag extends AbstractTag implements ControlTag {

	private Control _control;

	@Override
	public final Control getControl() {
		Control control = getExistingControl();
		if (control == null) {
			control = createControl();
			_control = control;
		}
		return control;
	}

	protected final Control getExistingControl() {
		return _control;
	}

	protected abstract Control createControl();

	@Override
	protected void teardown() {
		_control = null;
		super.teardown();
	}
}
