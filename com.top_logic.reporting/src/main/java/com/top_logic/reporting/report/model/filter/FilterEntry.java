/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.filter;

import java.util.HashMap;

import com.top_logic.basic.col.Filter;
import com.top_logic.reporting.report.model.partition.function.PartitionFunction;

/**
 * This class wraps a {@link Filter} and a i18n message used by a
 * {@link PartitionFunction} to sort business objects in appropriate partitions.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class FilterEntry {

	Filter filter;
	HashMap messages;
	String language;

	/**
	 * Creates a {@link FilterEntry}.
	 */
	public FilterEntry(Filter aFilter, String aLanguage, HashMap messages) {
		this.filter = aFilter;
		this.language = aLanguage;
		this.messages = messages;
	}

	/**
	 * Returns the {@link Filter}s.
	 */
	public Filter getFilter() {
		return this.filter;
	}

	/**
	 * Returns the current selected language of the report.
	 */
	public String getLanguage() {
		return this.language;
	}

	/**
	 * Returns the i18n messages. Keys: string languages (e.g. 'de' or 'en'). Values:
	 * strings
	 */
	public HashMap getMessages() {
		return this.messages;
	}
	
	public boolean accept(Object anObject) {
		return this.filter.accept(anObject);
	}
}

