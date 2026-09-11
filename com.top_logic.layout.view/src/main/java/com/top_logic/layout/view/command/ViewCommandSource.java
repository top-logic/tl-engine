/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.util.ResKey;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.ViewContext;

/**
 * A configured source of commands whose number and identity are known only at runtime.
 *
 * <p>
 * A {@link ViewCommand} is one command written in the view; a {@link ViewCommandSource} stands for
 * a whole set of them derived from state the view does not spell out - the configured UI themes,
 * the available languages, the entries of a registry. The set is asked for once per rendering of
 * the element carrying the source, so it reflects the state at that moment.
 * </p>
 *
 * @see ThemeCommands
 */
public interface ViewCommandSource {

	/**
	 * Configuration for {@link ViewCommandSource}.
	 */
	interface Config<I extends ViewCommandSource> extends PolymorphicConfiguration<I> {

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The heading a menu shows above this source's entries.
		 *
		 * <p>
		 * Unset, the source's own heading applies - the one naming the kind of choice it offers.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();
	}

	/**
	 * The heading a menu shows above this source's entries, or {@code null} for none.
	 *
	 * <p>
	 * The configured {@link Config#getLabel() label} where there is one, else the source's own.
	 * </p>
	 */
	ResKey getLabel();

	/**
	 * The commands to offer, in the order they are to appear.
	 *
	 * @param context
	 *        The context the carrying element is rendered in.
	 * @return The commands; empty when the source currently has nothing to offer.
	 */
	List<CommandModel> getCommands(ViewContext context);
}
