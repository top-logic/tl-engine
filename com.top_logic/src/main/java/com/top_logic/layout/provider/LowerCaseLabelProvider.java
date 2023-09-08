/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import java.util.Locale;

import com.top_logic.layout.LabelProvider;

/**
 * Transforming {@link LabelProvider} implementation that converts the labels
 * provided by a base {@link LabelProvider} to lower case.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LowerCaseLabelProvider implements LabelProvider {

	private final LabelProvider base;
	private final Locale locale;

	public LowerCaseLabelProvider(LabelProvider base, Locale locale) {
		this.base   = base;
		this.locale = locale;
	}

	@Override
	public String getLabel(Object object) {
		return base.getLabel(object).toLowerCase(locale);
	}

}
