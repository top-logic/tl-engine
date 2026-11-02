/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ViewCommand} that discards all in-memory modifications of the files opened in the
 * designer and shows the application's root view read anew.
 *
 * <p>
 * Reverts the {@link DesignSession} in the configured channel. The {@link DesignerTreeElement}
 * follows the session to the tree built anew.
 * </p>
 *
 * @see DesignSession#revert()
 */
public class RevertDesignCommand implements ViewCommand {

	/**
	 * Configuration for {@link RevertDesignCommand}.
	 */
	@TagName("revert-design")
	public interface Config extends ViewCommand.Config {

		@Override
		@ClassDefault(RevertDesignCommand.class)
		Class<? extends ViewCommand> getImplementationClass();

		/** Configuration name for {@link #getSession()}. */
		String SESSION = "session";

		/**
		 * Reference to the channel containing the {@link DesignSession}.
		 */
		@Name(SESSION)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getSession();
	}

	private final Config _config;

	/**
	 * Creates a new {@link RevertDesignCommand}.
	 */
	@CalledByReflection
	public RevertDesignCommand(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public HandlerResult execute(ReactContext context, Object input) {
		if (context instanceof ViewContext viewContext
			&& viewContext.resolveChannel(_config.getSession()).get() instanceof DesignSession session) {
			session.revert();
		}
		return HandlerResult.DEFAULT_RESULT;
	}
}
