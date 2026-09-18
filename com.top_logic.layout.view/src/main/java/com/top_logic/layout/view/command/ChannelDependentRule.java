/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Optional mix-in for a {@link ViewExecutabilityRule} whose answer depends on channels other than
 * the {@link ViewCommand.Config#getInput() input} of the command it guards.
 *
 * <p>
 * A command follows its input by itself, so a rule deciding by that value alone needs nothing here.
 * A rule deciding by state the command does not take as input - the step a surrounding wizard
 * displays, say - names the channels carrying that state, and the command's
 * {@link ViewCommandModel} follows them for as long as it is attached, re-evaluating the rules
 * whenever one of them takes a new value.
 * </p>
 *
 * <p>
 * A rule that resolves its channels from the context of the command combines this with
 * {@link ContextDependentRule}: {@link ContextDependentRule#bind(ViewContext) the binding}
 * happens first, so what it captures is what the channels are read from.
 * </p>
 */
public interface ChannelDependentRule {

	/**
	 * The channels whose values this rule decides by.
	 *
	 * @return The channels, empty for a rule that currently depends on none. Answered after the
	 *         rule is bound, and not asked again.
	 */
	List<ViewChannel> observedChannels();
}
