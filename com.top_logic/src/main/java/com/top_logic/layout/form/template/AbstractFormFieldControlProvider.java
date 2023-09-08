/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.LabelControl;

/**
 * Base class for {@link ControlProvider}s for {@link FormMember} models.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormFieldControlProvider implements ControlProvider {

	@Override
	public Control createControl(Object model, String style) {
		if (model == null) {
			return null;
		}

		if (!(model instanceof FormMember)) {
			return null;
		}

		FormMember member = (FormMember) model;
		if (FormTemplateConstants.STYLE_LABEL_VALUE.equals(style)) {
			return createLabel(member);
		}
		if (FormTemplateConstants.STYLE_LABEL_WITH_COLON_VALUE.equals(style)) {
			LabelControl label = createLabel(member);
			label.setColon(true);
			return label;
		}
		if (FormTemplateConstants.STYLE_ERROR_VALUE.equals(style)) {
			return createError(member);
		}

		if (!FormTemplateConstants.STYLE_DIRECT_VALUE.equals(style)) {
			ControlProvider cp = member.getControlProvider();
			if (cp != null && cp != this) {
				return cp.createControl(member, FormTemplateConstants.STYLE_DIRECT_VALUE);
			}
		}

		return createInput(member);
	}

	/**
	 * Creates a {@link LabelControl} for the given {@link FormMember}.
	 */
	protected LabelControl createLabel(FormMember member) {
		return new LabelControl(member);
	}

	/**
	 * Creates an error view for the given {@link FormMember}.
	 */
	protected Control createError(FormMember member) {
		return new ErrorControl(member, true);
	}

	/**
	 * Creates an input {@link Control} for the given {@link FormMember} based on the field type.
	 */
	protected abstract Control createInput(FormMember member);

}
