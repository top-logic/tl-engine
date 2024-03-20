/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.currency;

import java.util.Currency;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} for {@link Currency} values.
 */
public class CurrencyLabels implements LabelProvider {

	@Override
	public String getLabel(Object object) {
		return ((Currency) object).getDisplayName(ThreadContext.getLocale());
	}

}
