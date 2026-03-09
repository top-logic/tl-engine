/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A command that can be executed within the view system.
 *
 * <p>
 * View commands are the declarative replacement for legacy
 * {@link com.top_logic.tool.boundsec.CommandHandler CommandHandler}s. They are configured in
 * {@code .view.xml} files and rendered according to their {@link Config#getPlacement() placement}.
 * </p>
 */
public interface ViewCommand {

	/**
	 * Configuration for {@link ViewCommand}.
	 */
	interface Config extends PolymorphicConfiguration<ViewCommand> {

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getCssClasses()}. */
		String CSS_CLASSES = "css-classes";

		/** Configuration name for {@link #getPlacement()}. */
		String PLACEMENT = "placement";

		/** Configuration name for {@link #getClique()}. */
		String CLIQUE = "clique";

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getExecutability()}. */
		String EXECUTABILITY = "executability";

		/** Configuration name for {@link #getConfirmation()}. */
		String CONFIRMATION = "confirmation";

		/** Configuration name for {@link #getCheckDirty()}. */
		String CHECK_DIRTY = "check-dirty";

		/** Configuration name for {@link #getGroup()}. */
		String GROUP = "group";

		/**
		 * The programmatic name of this command.
		 *
		 * <p>
		 * Used for referencing the command in scripts and tests. If not set, the command can only
		 * be invoked through the UI.
		 * </p>
		 */
		@Name(NAME)
		@Nullable
		String getName();

		/**
		 * The user-visible label for this command.
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * The icon displayed for this command.
		 */
		@Name(IMAGE)
		@Nullable
		ThemeImage getImage();

		/**
		 * Additional CSS classes to apply to the command's UI element.
		 */
		@Name(CSS_CLASSES)
		@Nullable
		String getCssClasses();

		/**
		 * Where to render this command in the UI.
		 */
		@Name(PLACEMENT)
		CommandPlacement getPlacement();

		/**
		 * The clique name for grouping related commands.
		 *
		 * @see CommandCliques
		 */
		@Name(CLIQUE)
		@Nullable
		String getClique();

		/**
		 * Reference to a channel whose value is passed as input to the command.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Rules that determine when this command is executable.
		 */
		@Name(EXECUTABILITY)
		@EntryTag("rule")
		List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> getExecutability();

		/**
		 * Strategy for producing a confirmation dialog before execution.
		 *
		 * <p>
		 * If {@code null}, no confirmation is shown.
		 * </p>
		 */
		@Name(CONFIRMATION)
		@Nullable
		PolymorphicConfiguration<? extends ViewCommandConfirmation> getConfirmation();

		/**
		 * Scope of the dirty check to perform before executing this command.
		 */
		@Name(CHECK_DIRTY)
		DirtyCheckScope getCheckDirty();

		/**
		 * The command group for access control.
		 */
		@Name(GROUP)
		@Nullable
		CommandGroupReference getGroup();
	}

	/**
	 * Executes this command.
	 *
	 * @param context
	 *        The view display context providing rendering infrastructure.
	 * @param input
	 *        The input value from the configured channel (may be {@code null}).
	 * @return The result of the command execution.
	 */
	HandlerResult execute(ReactContext context, Object input);
}
