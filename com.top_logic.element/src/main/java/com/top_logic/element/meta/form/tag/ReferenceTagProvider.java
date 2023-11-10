/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;

/**
 * {@link DisplayProvider} for attributes referencing other objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReferenceTagProvider extends AbstractReferenceTagProvider {

	/**
	 * Singleton {@link ReferenceTagProvider} instance.
	 */
	public static final ReferenceTagProvider INSTANCE = new ReferenceTagProvider();

	private ReferenceTagProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		if (AttributeOperations.isComposition(editContext)) {
			return DefaultFormFieldControlProvider.INSTANCE.createControl(member);
		}

		return getReferenceDisplay(editContext, member);
	}

	@Override
	public HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		if (AttributeOperations.isComposition(editContext)) {
			return DefaultFormFieldControlProvider.INSTANCE.createFragment(member);
		}

		return getReferenceDisplayFragment(editContext, member);
	}

}
