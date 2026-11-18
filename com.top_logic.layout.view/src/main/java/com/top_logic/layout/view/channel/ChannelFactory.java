/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelPartRef;

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

	/**
	 * The {@link ChannelConfig#getType() declared value type} of the channel with the given
	 * configuration.
	 *
	 * @param context
	 *        The context reporting a type that does not exist or is no class.
	 * @param config
	 *        The configuration of the channel.
	 * @return The declared class, {@code null} if none is declared or the declaration is erroneous.
	 */
	static TLClass resolveValueType(InstantiationContext context, ChannelConfig config) {
		TLModelPartRef typeRef = config.getType();
		if (typeRef == null) {
			return null;
		}
		TLType type;
		try {
			type = typeRef.resolveType();
		} catch (RuntimeException ex) {
			context.error("The " + ChannelConfig.TYPE + " '" + typeRef.qualifiedName() + "' of channel '"
				+ config.getName() + "' does not exist.", ex);
			return null;
		}
		if (!(type instanceof TLClass clazz)) {
			context.error("The " + ChannelConfig.TYPE + " '" + typeRef.qualifiedName() + "' of channel '"
				+ config.getName() + "' is no class.");
			return null;
		}
		return clazz;
	}
}
