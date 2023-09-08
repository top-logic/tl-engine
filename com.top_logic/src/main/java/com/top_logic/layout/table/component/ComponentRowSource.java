/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import java.util.List;
import java.util.Map;

import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.RowsChannel;
import com.top_logic.layout.component.IComponent;
import com.top_logic.layout.component.Selectable;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutComponent} interface for table-like components providing access to the currently
 * displayed rows.
 * 
 * <p>
 * Note: In addition to implementing this interface, a {@link LayoutComponent} must also override
 * {@link LayoutComponent#channels()} and return {@link #MODEL_ROWS_AND_SELECTION_CHANNEL}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ComponentRowSource extends IComponent {

	/**
	 * Value to return from {@link LayoutComponent#channels()} implementation.
	 */
	Map<String, ChannelSPI> MODEL_ROWS_AND_SELECTION_CHANNEL =
		LayoutComponent.channels(Selectable.MODEL_AND_SELECTION_CHANNEL, RowsChannel.INSTANCE);

	/**
	 * The currently displayed rows of this component.
	 */
	List<?> getDisplayedRows();

	/**
	 * The rows channel.
	 */
	default ComponentChannel rowsChannel() {
		return getChannel(RowsChannel.NAME);
	}

}
