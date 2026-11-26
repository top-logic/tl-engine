/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.state.SideRailSectionsState;

/**
 * The column of a {@link ReactSideRailControl}: its sections stacked, each with a header and its
 * content, the component {@code TLSideRailSections}.
 *
 * <p>
 * A control of its own, so that the rail renders it as its column or inside its drawer.
 * </p>
 */
public final class ReactSideRailSectionsControl extends ReactControl {

	private static final String REACT_MODULE = "TLSideRailSections";

	private final ReactSideRailControl _rail;

	/**
	 * Creates the column of the given rail.
	 */
	ReactSideRailSectionsControl(ReactContext context, ReactSideRailControl rail) {
		super(context, null, REACT_MODULE);
		_rail = rail;
	}

	/**
	 * Writes the sections of the rail into the state.
	 */
	void writeSections() {
		putState(SideRailSectionsState.SECTIONS__PROP, _rail.sectionState(true));
	}

	/**
	 * Handles a click on the header of a section.
	 */
	@ReactCommandHandler(value = ReactSideRailControl.TOGGLE_SECTION_COMMAND,
		params = @ReactParam(name = ReactSideRailControl.NAME_ARG, type = "string", required = true,
			description = "Name of the section whose header was clicked."))
	void handleToggleSection(Map<String, Object> arguments) {
		_rail.handleToggleSection((String) arguments.get(ReactSideRailControl.NAME_ARG));
	}
}
