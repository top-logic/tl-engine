/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.basic.ControlCommand;

/**
 * {@link LayoutControl} that can be maximized within a {@link #getCockpit() cockpit container}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MaximizableControl extends AbstractMaximizableControl<MaximizableControl> {

	private static final Map<String, ControlCommand> NO_COMMANDS = Collections.<String, ControlCommand> emptyMap();

	/**
	 * Creates a {@link MaximizableControl}.
	 * 
	 * @param model
	 *        The model storing the expansion state.
	 */
	public MaximizableControl(Expandable model) {
		super(model, NO_COMMANDS);
	}

	@Override
	public MaximizableControl self() {
		return this;
	}
}
