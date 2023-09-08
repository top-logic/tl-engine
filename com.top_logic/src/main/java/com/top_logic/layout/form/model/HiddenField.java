/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.Map;

import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;


/**
 * A {@link HiddenField} provides the ability to attach an arbitrary application
 * value to a {@link FormContext}.
 * 
 * <p>
 * A {@link HiddenField} cannot be displayed at the user interface and it is
 * never included on the form page (not even as hidden input element). Including
 * a hidden field in the generated page is not necessary, because its is
 * remembered at the server side and the value can be recovered from the
 * {@link FormContext} by a lookup with the field's name.</p>
 * <p>This handling is useful to queue different command handler (e.g. import
 * of a project to the application, which contains different standard operations
 * which should be handled by the correct 
 * {@link com.top_logic.tool.boundsec.CommandHandler command handlers}). The
 * {@link com.top_logic.layout.form.component.AbstractCreateCommandHandler} provides a point
 * of usage in the method {@link com.top_logic.layout.form.component.AbstractCreateCommandHandler#createObject(LayoutComponent, Object, FormContainer, Map)}
 * which doesn't know the calling component.</p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HiddenField extends ConstantField {

	/**
	 * Creates a new {@link HiddenField} with value <code>null</code>.
	 * 
	 * @see FormFactory#newHiddenField(String, Object) for creating a {@link HiddenField} with
	 *      a pre-initialized value.
	 * 
	 * @param name
	 *        See {@link FormMember#getName()}
	 */
	protected HiddenField(String name) {
		super(name, !IMMUTABLE);
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitHiddenField(this, arg);
	}
}
