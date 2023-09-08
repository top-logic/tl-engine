/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.text.Collator;
import java.util.Comparator;

import com.top_logic.util.TLContext;

/**
 * Compares {@link CodeCompletion} by ascending names lexicographic.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CompletionByNameComparator implements Comparator<CodeCompletion> {

	/**
	 * Static instance of {@link CompletionByNameComparator}
	 */
	public static final CompletionByNameComparator INSTANCE = new CompletionByNameComparator();

	private Comparator<CodeCompletion> _comparator;

	private CompletionByNameComparator() {
		_comparator = createCompletionComparatorByName();
	}

	@Override
	public int compare(CodeCompletion completion1, CodeCompletion completion2) {
		return _comparator.compare(completion1, completion2);
	}

	private Comparator<CodeCompletion> createCompletionComparatorByName() {
		return Comparator.comparing(CodeCompletion::getName, createNullFirstLocaleComparator());
	}

	private Comparator<String> createNullFirstLocaleComparator() {
		return Comparator.nullsFirst(Collator.getInstance(TLContext.getLocale()));
	}

}
