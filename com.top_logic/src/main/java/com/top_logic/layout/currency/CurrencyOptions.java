/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.currency;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.thread.ThreadContext;

/**
 * Option provider for {@link Currency}-valued configuration properties.
 */
public class CurrencyOptions extends Function0<List<Currency>> {

	@Override
	public List<Currency> apply() {
		ArrayList<Currency> result = new ArrayList<>(Currency.getAvailableCurrencies());
		Collator collator = Collator.getInstance(ThreadContext.getLocale());
		result.sort((c1, c2) -> collator.compare(c1.getDisplayName(), c2.getDisplayName()));
		return result;
	}

}
