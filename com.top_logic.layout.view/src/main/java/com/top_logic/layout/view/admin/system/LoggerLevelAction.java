/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.ArrayList;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.logging.LogConfigurator;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.model.TLObject;

/**
 * {@link ViewAction} returning the configured loggers and their levels for display in a
 * {@link LoggerLevelTable}, and optionally adding or removing a logger configuration first.
 *
 * <p>
 * The returned {@code List<Map.Entry<String, String>>} (logger name to level name) is meant to be written
 * to the channel feeding the {@link LoggerLevelTable}. {@link Mode#ADD} configures the logger named in the
 * {@link Config#getModel() model channel} (a transient {@code tl.admin:NewLogger}) at the default level so
 * it can then be adjusted inline; {@link Mode#REMOVE} drops the explicit configuration of the logger held
 * by the {@link Config#getSelection() selection channel}, reverting it to its inherited level.
 * </p>
 */
public class LoggerLevelAction implements ViewAction {

	/** Level a newly added logger starts at; the inline editor then adjusts it. */
	private static final String DEFAULT_LEVEL = "INFO";

	/** Model attribute holding the name of the logger to add. */
	private static final String NAME = "name";

	/**
	 * What a {@link LoggerLevelAction} does before returning the logger list.
	 */
	public enum Mode {
		/** Just return the current logger snapshot. */
		REFRESH,

		/** Configure the logger named in the model channel at the default level. */
		ADD,

		/** Remove the explicit configuration of the selected logger. */
		REMOVE;
	}

	/**
	 * Configuration for {@link LoggerLevelAction}.
	 *
	 * <p>
	 * App-specific action, referenced by {@code class=} in the logger-levels view rather than claiming a
	 * global {@code @TagName}.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<LoggerLevelAction> {

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getModel()}. */
		String MODEL = "model";

		/** Configuration name for {@link #getSelection()}. */
		String SELECTION = "selection";

		@Override
		@ClassDefault(LoggerLevelAction.class)
		Class<? extends LoggerLevelAction> getImplementationClass();

		/**
		 * What the action does before returning the logger list.
		 */
		@Name(MODE)
		@Mandatory
		Mode getMode();

		/**
		 * Name of the channel holding the transient {@code tl.admin:NewLogger} input; required for
		 * {@link Mode#ADD}.
		 */
		@Name(MODEL)
		@Nullable
		String getModel();

		/**
		 * Name of the channel holding the selected logger name; required for {@link Mode#REMOVE}.
		 */
		@Name(SELECTION)
		@Nullable
		String getSelection();
	}

	private final Mode _mode;

	private final String _modelChannel;

	private final String _selectionChannel;

	/**
	 * Creates a new {@link LoggerLevelAction} from configuration.
	 */
	@CalledByReflection
	public LoggerLevelAction(InstantiationContext context, Config config) {
		_mode = config.getMode();
		_modelChannel = config.getModel();
		_selectionChannel = config.getSelection();
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		LogConfigurator configurator = LogConfigurator.getInstance();
		switch (_mode) {
			case ADD:
				add(context, configurator);
				break;
			case REMOVE:
				remove(context, configurator);
				break;
			case REFRESH:
				break;
		}
		return new ArrayList<>(configurator.getLoggerLevels().entrySet());
	}

	/**
	 * Configures the logger named in the model channel at the {@link #DEFAULT_LEVEL default level}.
	 */
	private void add(ReactContext context, LogConfigurator configurator) {
		TLObject model = channelValue(context, _modelChannel) instanceof TLObject object ? object : null;
		if (model != null && model.tValueByName(NAME) instanceof String name && !name.isBlank()) {
			configurator.setLoggerLevel(name.trim(), DEFAULT_LEVEL);
		}
	}

	/**
	 * Removes the explicit configuration of the logger held by the selection channel.
	 */
	private void remove(ReactContext context, LogConfigurator configurator) {
		if (channelValue(context, _selectionChannel) instanceof String name && !name.isEmpty()) {
			configurator.removeLoggerLevel(name);
		}
	}

	/**
	 * The current value of the named channel, or {@code null} when unavailable.
	 */
	private static Object channelValue(ReactContext context, String channel) {
		if (channel == null || !(context instanceof ViewContext viewContext) || !viewContext.hasChannel(channel)) {
			return null;
		}
		return viewContext.resolveChannel(new ChannelRef(channel)).get();
	}
}
