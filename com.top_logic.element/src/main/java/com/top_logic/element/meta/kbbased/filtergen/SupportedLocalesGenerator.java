/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.List;
import java.util.Locale;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.meta.form.EditContext;

/**
 * {@link ListGeneratorAdaptor} delivering the supported {@link Locale}s of the application.
 * 
 * @see ResourcesModule#getSupportedLocales()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SupportedLocalesGenerator extends ListGeneratorAdaptor {

	@Override
	public List<?> generateList(EditContext editContext) {
		return ResourcesModule.getInstance().getSupportedLocales();
	}

}

