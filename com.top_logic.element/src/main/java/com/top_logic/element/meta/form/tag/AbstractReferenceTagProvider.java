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
import com.top_logic.layout.form.control.ChoiceControl;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.tag.SelectionTableTag;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.model.annotate.ui.ReferencePresentation;

/**
 * {@link DisplayProvider} for attributes referencing other objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractReferenceTagProvider implements DisplayProvider {

	/**
	 * Creates a tag where elements are selected in a popup dialog.
	 */
	protected Control createPopupDisplay(EditContext editContext, FormMember member) {
		return PopupSelectTagProvider.INSTANCE.createDisplay(editContext, member);
	}

	/**
	 * Creates a tag where elements are selected in a popup dialog.
	 */
	protected HTMLFragment createPopupDisplayFragment(EditContext editContext, FormMember member) {
		return PopupSelectTagProvider.INSTANCE.createDisplayFragment(editContext, member);
	}

	/**
	 * Creates a choice display with the given {@link Orientation}.
	 */
	protected ChoiceControl createChoiceDisplay(FormMember member, Orientation orientation) {
		ChoiceControl result = new ChoiceControl((SelectField) member);
		result.setOrientation(orientation);
		return result;
	}

	/**
	 * Create a table display of an attribute.
	 */
	protected Control createTableDisplay(FormMember member) {
		SelectionTableTag tag = new SelectionTableTag();
		return tag.createControl(member);
	}

	/**
	 * Create a table display of an attribute.
	 */
	protected HTMLFragment createTableDisplayFragment(FormMember member) {
		SelectionTableTag tag = new SelectionTableTag();
		return tag.createFragment(member);
	}

	/**
	 * Creates a reference display according to the given {@link ReferencePresentation}.
	 */
	protected Control getReferenceDisplay(EditContext editContext, FormMember member) {
		ReferencePresentation presentation = AttributeOperations.getPresentation(editContext);
		switch (presentation) {
			case DROP_DOWN: {
				return SelectTagProvider.INSTANCE.createDisplay(editContext, member);
			}
			case RADIO:
				return createChoiceDisplay(member, Orientation.VERTICAL);
			case RADIO_INLINE:
				return createChoiceDisplay(member, Orientation.HORIZONTAL);
			case POP_UP: {
				return createPopupDisplay(editContext, member);
			}
			case TABLE: {
				if (editContext.inTableContext()) {
					return createPopupDisplay(editContext, member);
				}
				return createTableDisplay(member);
			}
			default:
				throw ReferencePresentation.unknownPresentation(presentation);
		}
	}

	/**
	 * Creates a reference display according to the given {@link ReferencePresentation}.
	 */
	protected HTMLFragment getReferenceDisplayFragment(EditContext editContext, FormMember member) {
		ReferencePresentation presentation = AttributeOperations.getPresentation(editContext);
		switch (presentation) {
			case DROP_DOWN: {
				return SelectTagProvider.INSTANCE.createDisplayFragment(editContext, member);
			}
			case RADIO:
				return createChoiceDisplay(member, Orientation.VERTICAL);
			case RADIO_INLINE:
				return createChoiceDisplay(member, Orientation.HORIZONTAL);
			case POP_UP: {
				return createPopupDisplayFragment(editContext, member);
			}
			case TABLE: {
				if (editContext.inTableContext()) {
					return createPopupDisplayFragment(editContext, member);
				}
				return createTableDisplayFragment(member);
			}
			default:
				throw ReferencePresentation.unknownPresentation(presentation);
		}
	}

}

