/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.table;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * {@link CommandHandler} {@link Selectable#clearSelection() clearing the selection} of it's
 * {@link Selectable selectable component}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ClearSelectionCommand extends PreconditionCommandHandler {

	/** Command ID for the {@link ClearSelectionCommand}. */
	public static final String COMMAND_ID = "clearSelection";

	/**
	 * Creates a new {@link ClearSelectionCommand}.
	 */
	public ClearSelectionCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (!(component instanceof Selectable)) {
			return new Hide();
		}
		Selectable selectable = (Selectable) component;
		return new Success() {

			@Override
			protected void doExecute(DisplayContext context) {
				selectable.clearSelection();
			}
		};
	}

}

