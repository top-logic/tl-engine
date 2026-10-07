/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.button.KeyStroke;
import com.top_logic.layout.react.control.button.KeyStrokeFormat;
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
	interface Config extends PolymorphicConfiguration<ViewCommand>, ExecutabilityConfig {

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getTooltip()}. */
		String TOOLTIP = "tooltip";

		/** Configuration name for {@link #getCssClasses()}. */
		String CSS_CLASSES = "css-classes";

		/** Configuration name for {@link #getPlacement()}. */
		String PLACEMENT = "placement";

		/** Configuration name for {@link #getDisplay()}. */
		String DISPLAY = "display";

		/** Configuration name for {@link #getClique()}. */
		String CLIQUE = "clique";

		/** Configuration name for {@link #getCheckDirty()}. */
		String CHECK_DIRTY = "check-dirty";

		/** Configuration name for {@link #getKey()}. */
		String KEY = "key";

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
		 * The tooltip shown on hover for this command's button.
		 */
		@Name(TOOLTIP)
		@Nullable
		ResKey getTooltip();

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
		 * How this command's button displays icon and label, e.g. {@code icon-only} for a compact
		 * icon button whose label becomes the tooltip.
		 *
		 * <p>
		 * If not set, the default of the enclosing command scope applies, and without one the
		 * button shows the icon (when configured) together with the label.
		 * </p>
		 */
		@Name(DISPLAY)
		@Nullable
		@NullDefault
		ButtonDisplayMode getDisplay();

		/**
		 * The clique name for grouping related commands.
		 *
		 * @see CommandCliques
		 */
		@Name(CLIQUE)
		@Nullable
		String getClique();

		/**
		 * Which unsaved changes this command asks about before it runs.
		 *
		 * <p>
		 * When a form in the checked scope holds unsaved changes, the user is asked whether to save
		 * or discard them, or to cancel the command. After saving or discarding, the command runs.
		 * By default, the command runs without asking.
		 * </p>
		 *
		 * @see DirtyCheckScope
		 */
		@Name(CHECK_DIRTY)
		@FormattedDefault(DirtyCheckScope.NONE_NAME)
		DirtyCheckScope getCheckDirty();

		/**
		 * The keyboard gesture that triggers this command, written as a key optionally prefixed
		 * with modifiers, e.g. {@code "Enter"}, {@code "Escape"}, {@code "Ctrl+S"} or
		 * {@code "Shift+ArrowDown"}.
		 *
		 * <p>
		 * When unset, dialogs derive conventional defaults: the sole or
		 * {@link com.top_logic.layout.react.control.button.ButtonAppearance#PRIMARY primary}
		 * button-bar command answers {@code Enter}, and the dialog's cancel action answers
		 * {@code Escape}.
		 * </p>
		 */
		@Name(KEY)
		@Nullable
		@Format(KeyStrokeFormat.class)
		KeyStroke getKey();
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

	/**
	 * Whether executing this command applies the values entered into the enclosing form.
	 *
	 * <p>
	 * Such a command is disabled while the form displays validation errors, since the form would
	 * reject it.
	 * </p>
	 *
	 * @see FormValid
	 */
	default boolean appliesFormState() {
		return false;
	}

	/**
	 * The executability this command brings of its own, decided by what the command knows about
	 * itself rather than by what its use site configured.
	 *
	 * <p>
	 * A command that cannot be carried out in certain circumstances - a dialog's cancel while the
	 * dialog holds a running command, say - says so here, and its button gives that reason instead
	 * of doing nothing when pressed. The rule is combined with the
	 * {@link Config#getExecutability() configured rules} and takes part in the same way: it is
	 * {@link ContextDependentRule#bind(com.top_logic.layout.view.ViewContext) bound} to the
	 * context of the command and {@link ObservableRule observed} while its button is attached.
	 * </p>
	 *
	 * @return A rule of this command's own, {@link ViewExecutabilityRule#ALWAYS_EXECUTABLE} for a
	 *         command that leaves the decision to its use site. A fresh instance per call, since a
	 *         bound rule belongs to the one command model it was built for.
	 */
	default ViewExecutabilityRule getIntrinsicRule() {
		return ViewExecutabilityRule.ALWAYS_EXECUTABLE;
	}

	/**
	 * The kind of action this command stands for, decided by what the command does.
	 *
	 * <p>
	 * {@link ButtonTone#DANGER} for a command that destroys or discards what the user has. There is
	 * no configuration for it: an author states what a command does, the command's UI shows it.
	 * </p>
	 *
	 * @return {@link ButtonTone#DEFAULT} for an ordinary command.
	 *
	 * @see ViewAction#getTone()
	 */
	default ButtonTone getTone() {
		return ButtonTone.DEFAULT;
	}
}
