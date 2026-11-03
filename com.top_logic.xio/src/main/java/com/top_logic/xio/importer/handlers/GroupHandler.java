/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link NestedImportHandler} that applies its content handlers to the current element.
 *
 * <p>
 * A group neither changes the scope nor consumes input beyond what its content handlers do. It
 * bundles a set of handlers under its {@link Handler.Config#getId() ID}, so that they can be
 * applied elsewhere with a {@link HandlerRef}, e.g. the attribute handlers of an
 * {@link ObjectCreate} in the {@link UnlessKnown.Config#getUpdate() update} of a known object.
 * </p>
 */
@Label("Group")
public class GroupHandler<C extends GroupHandler.Config<?>> extends NestedImportHandler<C> {

	/**
	 * Configuration options for {@link GroupHandler}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends GroupHandler<?>> extends NestedImportHandler.Config<I> {

		/**
		 * Tag name of {@link GroupHandler}.
		 */
		String TAG_NAME = "group";

	}

	/**
	 * Creates a {@link GroupHandler} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GroupHandler(InstantiationContext context, C config) {
		super(context, config);
	}

}
