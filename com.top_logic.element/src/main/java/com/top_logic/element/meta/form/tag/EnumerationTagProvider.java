/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ChecklistControl;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;
import com.top_logic.model.annotate.util.TLAnnotations;

/**
 * {@link DisplayProvider} for attributes {@link TLEnumeration}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EnumerationTagProvider extends AbstractReferenceTagProvider {

	/**
	 * Singleton {@link EnumerationTagProvider} instance.
	 */
	public static final EnumerationTagProvider INSTANCE = new EnumerationTagProvider();

	private EnumerationTagProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		ClassificationPresentation presentation = TLAnnotations.getClassificationPresentation(editContext);
		switch (presentation) {
			case CHECKLIST:
				return new ChecklistControl((SelectField) member);
			case DROP_DOWN:
				return SelectTagProvider.INSTANCE.createDisplay(editContext, member);
			case POP_UP:
				return createPopupDisplay(editContext, member);
			case RADIO:
				return createChoiceDisplay(member, Orientation.VERTICAL);
			case RADIO_INLINE:
				return createChoiceDisplay(member, Orientation.HORIZONTAL);
		}
		throw ClassificationPresentation.noSuchEnum(presentation);
	}

	@Override
	public HTMLFragment createDisplayFragment(EditContext editContext, FormMember member) {
		ClassificationPresentation presentation = TLAnnotations.getClassificationPresentation(editContext);
		switch (presentation) {
			case CHECKLIST:
				return new ChecklistControl((SelectField) member);
			case DROP_DOWN:
				return SelectTagProvider.INSTANCE.createDisplay(editContext, member);
			case POP_UP:
				return createPopupDisplay(editContext, member);
			case RADIO:
				return createChoiceDisplay(member, Orientation.VERTICAL);
			case RADIO_INLINE:
				return createChoiceDisplay(member, Orientation.HORIZONTAL);
			default:
				throw ClassificationPresentation.noSuchEnum(presentation);
		}
	}

}
