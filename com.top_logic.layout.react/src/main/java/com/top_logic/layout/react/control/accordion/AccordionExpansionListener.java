/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.accordion;

/**
 * Observer of the sections of a {@link ReactAccordionControl} being expanded and collapsed.
 *
 * @see ReactAccordionControl#addExpansionListener(AccordionExpansionListener)
 */
@FunctionalInterface
public interface AccordionExpansionListener {

	/**
	 * Called after a section of the accordion was expanded or collapsed, whatever caused it: the
	 * user, an {@link ReactAccordionControl#isExclusive() exclusive} accordion collapsing the
	 * section expanded before, the section being revealed, or a programmatic change.
	 *
	 * @param accordion
	 *        The accordion the section belongs to.
	 * @param sectionId
	 *        The {@link AccordionSection#getId() id} of the section.
	 * @param expanded
	 *        Whether the section is now expanded.
	 */
	void onExpansionChanged(ReactAccordionControl accordion, String sectionId, boolean expanded);

}
