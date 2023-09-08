/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * {@link ModelProvider} retrieving {@link TLContext#getCurrentPersonWrapper()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CurrentUserModelProvider implements ModelProvider {

	/**
	 * Singleton {@link CurrentUserModelProvider} instance.
	 */
	public static final CurrentUserModelProvider INSTANCE = new CurrentUserModelProvider();

	private CurrentUserModelProvider() {
		// Singleton constructor.
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return TLContext.getContext().getCurrentPersonWrapper();
	}

}
