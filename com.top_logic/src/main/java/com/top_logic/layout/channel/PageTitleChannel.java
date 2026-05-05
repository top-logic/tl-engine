/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import com.top_logic.layout.WindowScope;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * {@link ChannelSPI} for the {@link MainLayout} page title channel.
 *
 * <p>
 * The channel value is converted to a label and forwarded to
 * {@link WindowScope#setPageTitle(String)}, which updates the browser tab title.
 * A channel value of <code>null</code> restores the title configured on the
 * top-level {@link LayoutComponent}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PageTitleChannel {

	/**
	 * Name of {@link PageTitleChannel}.
	 */
	public static final String NAME = "pageTitle";

	/**
	 * {@link ChannelSPI} for the {@link MainLayout} page title.
	 */
	public static final ChannelSPI INSTANCE = new DefaultChannelSPI(NAME, null);

}
