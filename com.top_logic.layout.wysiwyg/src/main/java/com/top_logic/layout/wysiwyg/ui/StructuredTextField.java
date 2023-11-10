/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.util.Resources;

/**
 * Field containing a {@link StructuredText} value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructuredTextField extends HiddenField {

	private static final Constraint NOT_EMPTY_STRUCTURED_TEXT = new AbstractConstraint() {

		@Override
		public boolean check(Object value) throws CheckException {
			if (value == null) {
				throw createNotEmptyException();
			}
			StructuredText html = (StructuredText) value;
			if (StringServices.isEmpty(html.getSourceCode())) {
				throw createNotEmptyException();
			}
			return true;
		}

		private CheckException createNotEmptyException() {
			return new CheckException(Resources.getInstance().getString(
				com.top_logic.layout.form.I18NConstants.NOT_EMPTY));
		}

	};
	/**
	 * Creates a {@link StructuredTextField}.
	 *
	 * @param name
	 *        See {@link #getName()}
	 */
	protected StructuredTextField(String name) {
		super(name);
	}

	@Override
	protected Constraint mandatoryConstraint() {
		return NOT_EMPTY_STRUCTURED_TEXT;
	}
}
