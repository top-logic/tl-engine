/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import java.util.Collections;

import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.DefaultChannelSPI;

/**
 * The diagram channel of a {@link FlowChartComponent}.
 *
 * <p>
 * This channel contains the {@link Diagram} that is currently displayed by the component.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DiagramChannel {

	/**
	 * Name of {@link DiagramChannel}.
	 */
	public static final String NAME = "diagram";

	/**
	 * Singleton {@link DiagramChannel} instance.
	 */
	public static final ChannelSPI INSTANCE = new DefaultChannelSPI(NAME, null, Collections.emptySet());

}
