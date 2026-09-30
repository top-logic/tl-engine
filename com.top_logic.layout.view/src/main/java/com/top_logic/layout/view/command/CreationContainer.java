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
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * Configuration of an action creating an object in the context of another object, the container.
 *
 * <p>
 * Without a {@link #getContainer() container}, the object is created at top level: the right to
 * create it is checked against the security root. With a container, it is checked in the context
 * of the container and, with a {@link #getReference() reference}, together with the right to write
 * that reference of the container.
 * </p>
 */
public interface CreationContainer extends ConfigurationItem {

	/** Configuration name for {@link #getContainer()}. */
	String CONTAINER = "container";

	/** Configuration name for {@link #getReference()}. */
	String REFERENCE = "reference";

	/**
	 * Channel holding the object in whose context the object is created.
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
}
