/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * An application's own citation syntax, as a test writes one: record codes in parentheses,
 * {@code (FACT-001, DEC-002)}, the way a consulting application cites its records.
 */
public class RecordCodeMarkers implements CitationMarkerSyntax {

	/** Configuration of {@link RecordCodeMarkers}. */
	public interface Config extends PolymorphicConfiguration<RecordCodeMarkers> {
		// No properties.
	}

	private static final Pattern CODES =
		Pattern.compile("\\s?\\(([A-Z]+-\\d{3}(?:,\\s*[A-Z]+-\\d{3})*)\\)");

	/** Creates the syntax from configuration. */
	public RecordCodeMarkers(InstantiationContext context, Config config) {
		// Nothing configurable.
	}

	@Override
	public List<Marker> find(String text) {
		List<Marker> result = new ArrayList<>();
		Matcher codes = CODES.matcher(text);
		while (codes.find()) {
			List<String> references = new ArrayList<>();
			for (String code : codes.group(1).split(",")) {
				references.add(code.trim());
			}
			result.add(new Marker(codes.start(), codes.end(), "", references, ""));
		}
		return result;
	}
}
