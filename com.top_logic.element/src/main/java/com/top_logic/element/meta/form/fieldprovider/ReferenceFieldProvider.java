/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.FormMember;

/**
 * {@link FieldProvider} for reference attributes, dispatching to an implementation depending on
 * whether the reference is a composition reference or not.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReferenceFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		return delegate(editContext).getFormField(editContext, fieldName);
	}

	private FieldProvider delegate(EditContext editContext) {
		return ReferenceFieldProviders.Module.INSTANCE.getImplementationInstance().delegate(editContext);
	}

	@Override
	public boolean renderWholeLine(EditContext editContext) {
		return delegate(editContext).renderWholeLine(editContext);
	}

}

