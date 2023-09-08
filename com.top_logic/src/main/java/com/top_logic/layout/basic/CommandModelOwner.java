/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;

/**
 * Used by the scripting framework to find the owner of a {@link CommandModel}.
 * 
 * <p>
 * The owner is needed to retrieve/find the {@link CommandModel} when the script is executed. When
 * there is no owner, use {@link #NO_OWNER}. But you have to either register another
 * {@link ModelNamingScheme} for the {@link CommandModel} to allow script recording.
 * </p>
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface CommandModelOwner {

	/**
	 * When there is no {@link CommandModelOwner} for a {@link CommandModel}.
	 * <p>
	 * If this is used, the {@link CommandModel} cannot be recorded for scripted tests. (See
	 * {@link CommandModelOwner} for details.)
	 * </p>
	 */
	public static final CommandModelOwner NO_OWNER = new CommandModelOwner() {

		@Override
		public CommandModel getCommandModel() {
			throw new UnsupportedOperationException("There is no owner and therefore no command model.");
		}
	};

	/**
	 * {@link CommandModel}, provided by this {@link CommandModelOwner}.
	 */
	CommandModel getCommandModel();

}
