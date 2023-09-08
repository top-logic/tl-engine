/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormMemberVisitor;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * Find the {@link LabelProvider} to create decoration info for.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LabelProviderVisitor extends AbstractFormMemberVisitor<LabelProvider, Void> {

	/** Singleton {@link LabelProviderVisitor} instance. */
	public static final LabelProviderVisitor INSTANCE = new LabelProviderVisitor();

	private LabelProviderVisitor() {
		// singleton instance
	}

	@Override
	public LabelProvider visitSelectField(SelectField member, Void arg) {
		return member.getOptionLabelProvider();
	}

	@Override
	public LabelProvider visitFormMember(FormMember member, Void arg) {
		return MetaLabelProvider.INSTANCE;
	}

}

