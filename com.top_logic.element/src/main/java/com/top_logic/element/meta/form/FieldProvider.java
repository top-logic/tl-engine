/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;

/**
 * Factory for {@link FormField}s for object attributes.
 * 
 * <p>
 * Must be implemented through {@link AbstractFieldProvider}.
 * </p>
 * 
 * @see DisplayProvider The factory for creating the concrete view of the form model created by a
 *      field provider.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FieldProvider extends Unimplementable {

	/**
	 * Create a {@link FormMember} for the given edit location.
	 * 
	 * @param editContext
	 *        Description of the edit location. An edit location is e.g. the value entered for an
	 *        attribute of some {@link EditContext#getObject() object} being displayed in a form.
	 * @param fieldName
	 *        The {@link FormMember#getName() field name} that should be used for the created field,
	 *        see {@link FormMember#getName()}.
	 * @return The newly created field that is able to display and edit a value of the
	 *         {@link EditContext#getValueType() type} of the edit location.
	 */
	FormMember getFormField(EditContext editContext, String fieldName);

	/**
	 * Whether the {@link FormField} created for the the given {@link EditContext} should be
	 * rendered over the whole form.
	 * 
	 * @param editContext
	 *        The context of the form.
	 * @return Whether the created field needs the whole width of the form.
	 * 
	 * @see #getFormField(EditContext, String)
	 */
	default boolean renderWholeLine(EditContext editContext) {
		return false;
	}

}
