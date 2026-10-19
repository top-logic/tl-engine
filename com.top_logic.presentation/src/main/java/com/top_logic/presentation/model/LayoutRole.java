/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * Purpose of a slide layout, independent of the names of the layouts of a template.
 */
public enum LayoutRole implements DataEnum {

	/** The title slide of a presentation. */
	TITLE("title"),

	/** A slide with a title and content. */
	CONTENT("content"),

	/** The agenda of a presentation. */
	AGENDA("agenda"),

	/** The first slide of a section. */
	SECTION("section"),

	/** The last slide of a presentation. */
	CLOSING("closing");

	private final String _id;

	LayoutRole(String id) {
		_id = id;
	}

	@Override
	public String id() {
		return _id;
	}

}
