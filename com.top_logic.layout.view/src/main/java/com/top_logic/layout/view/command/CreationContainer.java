/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.model.TLObject;

/**
 * Configuration of an action creating an object in the context of another object, the container.
 *
 * <p>
 * Without a {@link #getContainer() container}, the object is created at top level: the right to
 * create it is checked against the security root. With a container, the object is created in the
 * context of the container, and the right to create it is checked in that context and, with a
 * {@link #getReference() reference}, together with the right to write that reference of the
 * container.
 * </p>
 */
public interface CreationContainer extends ConfigurationItem {

	/** Configuration name for {@link #getContainer()}. */
	String CONTAINER = "container";

	/** Configuration name for {@link #getReference()}. */
	String REFERENCE = "reference";

	/**
	 * Channel holding the object in whose context the object is created.
	 *
	 * <p>
	 * The container is the create context of the object: default values of the object's
	 * attributes are computed in its context.
	 * </p>
	 */
	@Name(CONTAINER)
	@Nullable
	@Format(ChannelRefFormat.class)
	ChannelRef getContainer();

	/**
	 * Name of the (composition) reference of the {@link #getContainer() container} the created
	 * object belongs to.
	 *
	 * <p>
	 * The name is looked up in the type of the container. Requires a container.
	 * </p>
	 */
	@Name(REFERENCE)
	@Nullable
	String getReference();

	/**
	 * Reports a {@link #getReference() reference} given without a {@link #getContainer()
	 * container}.
	 */
	static void checkContainer(InstantiationContext context, CreationContainer config) {
		if (config.getReference() != null && config.getContainer() == null) {
			context.error("The '" + REFERENCE + "' '" + config.getReference() + "' requires a '" + CONTAINER
				+ "' at " + config.location() + ".");
		}
	}

	/**
	 * The current value of the {@link #getContainer() container} channel.
	 *
	 * @param context
	 *        The context resolving the channel.
	 * @param config
	 *        The configuration of the action.
	 * @param tagName
	 *        The tag name of the action, for the error report.
	 * @return The container, or <code>null</code> when no container is configured or the channel
	 *         is empty.
	 * @throws IllegalArgumentException
	 *         If the channel holds something else than an object.
	 */
	static TLObject resolveContainer(ReactContext context, CreationContainer config, String tagName) {
		ChannelRef channel = config.getContainer();
		if (channel == null) {
			return null;
		}
		Object value = ((ViewContext) context).resolveChannel(channel).get();
		if (value == null) {
			return null;
		}
		if (!(value instanceof TLObject container)) {
			throw new IllegalArgumentException("The '" + CONTAINER + "' of a '" + tagName
				+ "' action holds no object: " + value);
		}
		return container;
	}
}
