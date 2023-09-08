/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link AbstractFormMemberTag} linking its created control to the context component for reuse.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormMemberControlTag extends AbstractFormMemberTag implements ControlTag, ControlProvider {

	private Control _control;

	@Override
	public final Control createControl(Object model, String displayStyle) {
		return createControl((FormMember) model, displayStyle);
	}

	/**
	 * Creates a new {@link Control} that renders the contents of this tag.
	 * 
	 * @param member
	 *        The field, for which a new {@link Control} should be created.
	 * @param displayStyle
	 *        See {@link ControlProvider#createControl(Object, String)}.
	 * @return The newly created {@link Control}.
	 */
	public abstract Control createControl(FormMember member, String displayStyle);

	@Override
	public Control getControl() {
		if (_control == null) {
			_control = createControl(getMember());
		}
		return _control;
	}

	/**
	 * Fetch the rendering control, if it was already created.
	 */
	protected final Control existingControl() {
		return _control;
	}

	@Override
	protected void teardown() {
		_control = null;

		// Clear the id since this is not done by the TagSupport implementation.
		// This is essential, because new identifiers are only assigned, if
		// there is not already an identifier set. Otherwise, if the tag is
		// reused, an identifier would be assigned multiple times.
		setId(null);

		super.teardown();
	}
}
