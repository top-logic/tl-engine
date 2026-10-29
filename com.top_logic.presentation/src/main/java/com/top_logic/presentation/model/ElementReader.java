/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * Reader of an element type of a presentation description.
 *
 * <p>
 * The readers are registered by their type name in {@link DescriptionReader}.
 * </p>
 */
@FunctionalInterface
public interface ElementReader {

	/**
	 * Creates the element described by the given entries.
	 *
	 * <p>
	 * The entry {@link com.top_logic.presentation.data.PresentationData#TYPE} has already been
	 * read. All entries the element supports must be requested from the given {@link Entries},
	 * also those not given, so that the caller can report the remaining entries as unknown.
	 * </p>
	 *
	 * @param entries
	 *        The entries of the element's dictionary, knowing their path in the description.
	 * @return The element.
	 */
	ElementSpec read(Entries entries);

}
