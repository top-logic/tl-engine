/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.DefaultChannelSPI;
import com.top_logic.layout.channel.ModelChannel;
import com.top_logic.layout.channel.RowsChannel;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Channel that holds the list of displayed columns of a table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ColumnsChannel {

	/**
	 * Name of the columns channel.
	 * 
	 * @see #INSTANCE
	 */
	String NAME = "columns";

	/**
	 * Column channel instance.
	 * 
	 * <p>
	 * Users of the channel must {@link ComponentChannel#set(Object)} either <code>null</code> which
	 * means "no columns" or a {@link List} of {@link String}s, the displayed list of column named
	 * of the table.
	 * </p>
	 */
	ChannelSPI INSTANCE = new DefaultChannelSPI(NAME, null);

	/**
	 * Channel definitions for components supporting channels
	 * <ul>
	 * <li>{@link ModelChannel#INSTANCE}</li>
	 * <li>{@link SelectionChannel#INSTANCE}</li>
	 * <li>{@link RowsChannel#INSTANCE}</li>
	 * <li>{@link ColumnsChannel#INSTANCE}</li>
	 * </ul>
	 */
	Map<String, ChannelSPI> COLUMNS_MODEL_ROWS_AND_SELECTION_CHANNEL = LayoutComponent
		.channels(ComponentRowSource.MODEL_ROWS_AND_SELECTION_CHANNEL, ColumnsChannel.INSTANCE);

	/**
	 * Channels containing the {@link LayoutComponent#MODEL_CHANNEL} and the
	 * {@link ColumnsChannel#INSTANCE}.
	 * 
	 * <p>
	 * Note: This is legacy quirks for components that use the model also as selection.
	 * </p>
	 */
	Map<String, ChannelSPI> COLUMNS_MODEL_AND_SELECTION_CHANNEL =
		LayoutComponent.channels(LayoutComponent.MODEL_CHANNEL, ColumnsChannel.INSTANCE);

	/**
	 * Configuration for components that supports the {@link ColumnsChannel#NAME column
	 * channel}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Definition of the target channel to connect the columns channel to.
		 */
		@Name(NAME)
		ModelSpec getColumns();

		/**
		 * Setter for {@link #getColumns()}.
		 */
		void setColumns(ModelSpec value);

	}

}

