/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.model.AbstractFormMemberVisitor;

/**
 * Base class to easily customize the view of certain types of {@link FormMember}s.
 * 
 * <p>
 * Subclasses can override methods from the {@link FormMemberVisitor} interface to create special
 * controls for certain types of form members.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormControlProviderAdapter extends AbstractFormMemberVisitor<HTMLFragment, String>
		implements ControlProvider {

	private final ControlProvider _fallback;

	/**
	 * Create a {@link FormControlProviderAdapter}.
	 * 
	 * @param inner
	 *        See {@link #getFallback()}.
	 */
	public FormControlProviderAdapter(ControlProvider inner) {
		_fallback = inner;
	}

	/**
	 * The fall-back {@link ControlProvider} to use, if no special case of this class matches a
	 * {@link FormField}.
	 */
	public ControlProvider getFallback() {
		return _fallback;
	}

	@Override
	public HTMLFragment visitFormMember(FormMember member, String arg) {
		return _fallback.createFragment(member, arg);
	}

	@Override
	public Control createControl(Object model, String style) {
		return (Control) createFragment(model, style);
	}

	@Override
	public HTMLFragment createFragment(Object model, String style) {
		return ((FormMember) model).visit(this, style);
	}

}
