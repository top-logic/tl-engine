/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.layout.Card;

/**
 * {@link ControlProvider} that returns the {@link Card#getContent()} of a {@link Card} as
 * {@link Control}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CardControlContent implements ControlProvider {

	/**
	 * Singleton {@link CardControlContent} instance.
	 */
	public static final CardControlContent INSTANCE = new CardControlContent();

	private CardControlContent() {
		// Singleton constructor.
	}

	@Override
	public Control createControl(Object model, String style) {
		return (Control) ((Card) model).getContent();
	}

}