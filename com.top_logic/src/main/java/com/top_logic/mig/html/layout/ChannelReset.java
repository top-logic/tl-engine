/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.layout.channel.ComponentChannel;

/**
 * Calls {@link ComponentChannel#resetListeners()} in all {@link LayoutComponent}s in the visited
 * tree.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public final class ChannelReset extends DefaultDescendingLayoutVisitor {

	/**
	 * Singleton {@link ChannelReset} instance.
	 */
	public static final ChannelReset INSTANCE = new ChannelReset();

	private ChannelReset() {
		// Singleton constructor.
	}

	@Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
		for (String channel : aComponent.channels().keySet()) {
			aComponent.getChannel(channel).resetListeners();
		}

		return super.visitLayoutComponent(aComponent);
	}
}
