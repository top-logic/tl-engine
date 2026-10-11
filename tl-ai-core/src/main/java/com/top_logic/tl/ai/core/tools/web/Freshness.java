/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.Locale;

/**
 * How recent the results of a web search must be.
 */
public enum Freshness {

	/** Published in the last 24 hours. */
	DAY,

	/** Published in the last 7 days. */
	WEEK,

	/** Published in the last 31 days. */
	MONTH,

	/** Published in the last 365 days. */
	YEAR;

	/** The name the model writes for this value, e.g. {@code week}. */
	public String externalName() {
		return name().toLowerCase(Locale.ROOT);
	}

	/**
	 * The value the model wrote.
	 *
	 * @param name
	 *        One of {@code day}, {@code week}, {@code month}, {@code year}, in any case.
	 * @return The value, or {@code null} when {@code name} is none of them.
	 */
	public static Freshness of(String name) {
		for (Freshness value : values()) {
			if (value.externalName().equalsIgnoreCase(name.strip())) {
				return value;
			}
		}
		return null;
	}
}
