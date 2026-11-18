/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Base configuration for channel declarations in a view.
 *
 * <p>
 * This is polymorphic so that different channel kinds (value channels, derived channels) can extend
 * it. The implementation class determines the {@link ChannelFactory} behavior at runtime.
 * </p>
 */
public interface ChannelConfig extends PolymorphicConfiguration<ChannelFactory> {

	/** Configuration name for {@link #getName()}. */
	String NAME = "name";

	/** Configuration name for {@link #getType()}. */
	String TYPE = "type";

	/**
	 * The unique name of this channel within the view.
	 */
	@Name(NAME)
	@Mandatory
	String getName();

	/**
	 * The model class of the values of this channel.
	 *
	 * <p>
	 * The declaration is used for the access check of a command working on the channel: while the
	 * channel is empty, a command whose action checks the access on its input itself (e.g. one
	 * deleting the object of the channel) is hidden when the user may perform the operation on no
	 * object of this type, and disabled otherwise. With an object in the channel, the object
	 * decides. The values of the channel are not checked against the type.
	 * </p>
	 *
	 * <p>
	 * Empty (default) for a channel without declared type. A type that is no class is a
	 * configuration error.
	 * </p>
	 *
	 * @see ViewChannel#getValueType()
	 */
	@Name(TYPE)
	@Nullable
	TLModelPartRef getType();
}
