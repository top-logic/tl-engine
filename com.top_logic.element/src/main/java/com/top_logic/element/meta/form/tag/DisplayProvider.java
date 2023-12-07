/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * Factory for the concrete display of an edit location.
 * 
 * @see #createDisplay(EditContext, FormMember)
 */
public interface DisplayProvider {

	/**
	 * Creates the display of the current edit location.
	 * 
	 * <p>
	 * An edit location is defined by the {@link EditContext#getObject() object being edited} and
	 * the {@link EditContext#getValueType() type of the value} being entered at that location. In
	 * the simplest case, an attribute of the edited object defines the edit location for the value
	 * of that attribute stored for the context object.
	 * </p>
	 *
	 * @param editContext
	 *        The edit location.
	 * @param member
	 *        The form model created for the current edit location, see
	 *        {@link FieldProvider#getFormField(EditContext, String)}.
	 * @return The display to render.
	 * 
	 * @implNote If the implementation of this method dispatches to another {@link ControlProvider},
	 *           be sure to use {@link FormTemplateConstants#STYLE_DIRECT_VALUE} as style for
	 *           calling {@link ControlProvider#createControl(Object, String)} to avoid a stack
	 *           overflow error, because the {@link ControlProvider} might otherwise dispatch the
	 *           call back to the {@link ControlProvider} annotated to the field.
	 */
	Control createDisplay(EditContext editContext, FormMember member);

	/**
	 * @deprecated Compatibility code re-adding a {@link ControlProvider} indirection.
	 */
	@Deprecated
	default ControlProvider getControlProvider(EditContext editContext) {
		return new AbstractFormFieldControlProvider() {
			@Override
			protected Control createInput(FormMember member) {
				return createDisplay(editContext, member);
			}
		};
	}

}
