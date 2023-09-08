/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.model.annotate.ui.ReferencePresentation;

/**
 * {@link DisplayProvider} for attributes referencing DAP objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DAPTagProvider extends AbstractReferenceTagProvider {

	/**
	 * Singleton {@link DAPTagProvider} instance.
	 */
	public static final DAPTagProvider INSTANCE = new DAPTagProvider();

	private DAPTagProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		ReferencePresentation presentation = AttributeOperations.getPresentation(editContext);
		switch (presentation) {
			case RADIO:
				return createChoiceDisplay(member, Orientation.VERTICAL);
			case RADIO_INLINE:
				return createChoiceDisplay(member, Orientation.HORIZONTAL);
			default:
				return createPopupDisplay(editContext, member);
		}
	}
}
