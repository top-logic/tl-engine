/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} which returns the label of a {@link FormMember}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormMemberLabelProvider implements LabelProvider {

	public static final LabelProvider INSTANCE = new FormMemberLabelProvider();

	private FormMemberLabelProvider() {
		// singleton instance
	}

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return StringServices.EMPTY_STRING;
		}
		return ((FormMember) object).getLabel();
	}

}

