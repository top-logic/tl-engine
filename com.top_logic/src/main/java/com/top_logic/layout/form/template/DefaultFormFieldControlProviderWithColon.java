/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.LabelControl;

/**
 * {@link DefaultFormFieldControlProvider} but using colons after labels.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultFormFieldControlProviderWithColon extends DefaultFormFieldControlProvider {

	/**
	 * Singleton {@link DefaultFormFieldControlProviderWithColon} instance.
	 */
	public static final DefaultFormFieldControlProviderWithColon INSTANCE =
		new DefaultFormFieldControlProviderWithColon();

	private DefaultFormFieldControlProviderWithColon() {
		// Singleton constructor.
	}

	@Override
	protected LabelControl createLabel(FormMember member) {
		LabelControl result = super.createLabel(member);
		result.setColon(true);
		return result;
	}

}
