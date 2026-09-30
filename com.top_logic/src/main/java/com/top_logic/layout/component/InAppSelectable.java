/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Extension of {@link Selectable} for in app configurable components.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface InAppSelectable extends Selectable {

	/**
	 * {@link Selectable.SelectableConfig} for components that are configurable in app.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface InAppSelectableConfig extends Selectable.SelectableConfig {

		/** @see #getOnSelectionChange() */
		String On_SELECTION_CHANGE = "onSelectionChange";

		/**
		 * Command that is executed when the selection of the component changes.
		 */
		@Name(On_SELECTION_CHANGE)
		@Label("Operation after selection change")
		CommandHandler.ConfigBase<? extends CommandHandler> getOnSelectionChange();
	}

	/**
	 * {@link CommandHandler} that is executed after value of {@link #selectionChannel()} changes.
	 *
	 * @return May be <code>null</code>.
	 */
	CommandHandler getOnSelectionHandler();

	@Override
	default void linkSelectionChannel(Log log) {
		Selectable.super.linkSelectionChannel(log);
		CommandHandler onSelectionHandler = getOnSelectionHandler();
		if (onSelectionHandler != null) {
			selectionChannel().addListener(new CommandHandlerChannelListener(onSelectionHandler, self()));
		}
	}

}
