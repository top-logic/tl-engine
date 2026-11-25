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
 * constructor.
 * </p>
 *
 * <p>
 * Per session, a channel is set up in two steps: {@link #createChannel(ViewContext)} creates it,
 * and {@link #bindChannel(ViewContext, ViewChannel)} connects it to the channels it is computed
 * from and gives it its initial value. A view first creates and registers all its channels, then
 * binds them, so that a channel may read a channel declared after it, and two channels may read
 * each other.
 * </p>
 */
public interface ChannelFactory {

	/**
	 * Creates the {@link ViewChannel} for this session, not yet connected to other channels.
	 *
	 * <p>
	 * The channel is ready to use after {@link #bindChannel(ViewContext, ViewChannel)}. Until then,
	 * it may be registered in the context and named as input of other channels, but it does not
	 * resolve channels itself and holds no computed value.
	 * </p>
	 *
	 * @param context
	 *        The view context for session-scoped state.
	 * @return A new channel instance.
	 */
	ViewChannel createChannel(ViewContext context);

	/**
	 * Connects a channel {@link #createChannel(ViewContext) created} by this factory to the
	 * channels it reads, and gives it its initial value.
	 *
	 * <p>
	 * The channels the given one reads may not be bound yet. A channel announces the value it is
	 * bound with to its listeners if it differs from the value it held before, so a channel bound
	 * earlier that reads it computes again.
	 * </p>
	 *
	 * @param context
	 *        The view context resolving the channel references.
	 * @param channel
	 *        The channel created by {@link #createChannel(ViewContext)} of this factory in the same
	 *        context.
	 */
	default void bindChannel(ViewContext context, ViewChannel channel) {
		// A channel reading no other channel is ready when created.
	}

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
