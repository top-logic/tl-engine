/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.accordion;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.react.control.ReactCommand;

/**
 * Typed arguments of the {@link ReactAccordionControl#TOGGLE_SECTION_COMMAND} command: which section
 * of an accordion to expand or collapse.
 *
 * <p>
 * The {@link Label} doubles as the {@link com.top_logic.layout.form.values.edit.ConfigLabelProvider}
 * template that renders a recorded step for humans.
 * </p>
 */
@Label("Set section '{sectionId}' expanded to {expanded}")
public interface ToggleSectionArguments extends ReactCommand {

	/** @see #getSectionId() */
	String SECTION_ID = "sectionId";

	/** @see #isExpanded() */
	String EXPANDED = "expanded";

	/**
	 * The {@link AccordionSection#getId() id} of the section to expand or collapse (one of the
	 * sections of this accordion).
	 */
	@Name(SECTION_ID)
	@Mandatory
	String getSectionId();

	/**
	 * {@code true} to expand the section, {@code false} to collapse it.
	 */
	@Name(EXPANDED)
	@Mandatory
	boolean isExpanded();

}
