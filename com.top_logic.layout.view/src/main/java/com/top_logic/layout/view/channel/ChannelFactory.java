/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.layout.view.ViewContext;

/**
 * Config-time factory for creating per-session {@link ViewChannel} instances.
 *
 * <p>
 * Instantiated once via {@link com.top_logic.basic.config.PolymorphicConfiguration} at
 * configuration parse time. Expensive operations (e.g. expression compilation) happen in the
 * constructor. {@link #createChannel(ViewContext)} is called per-session to produce wired channel
 * instances.
 * </p>
 */
public interface ChannelFactory {

	/**
	 * Creates a wired, ready-to-use {@link ViewChannel} for this session.
	 *
	 * @param context
	 *        The view context for resolving channel references and other session-scoped state.
	 * @return A new channel instance.
	 */
	ViewChannel createChannel(ViewContext context);
}
