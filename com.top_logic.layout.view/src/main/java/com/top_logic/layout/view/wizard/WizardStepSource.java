/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Contributes steps to a {@link WizardElement &lt;wizard&gt;}.
 *
 * <p>
 * The step sequence of a wizard is the concatenation of what its sources contribute, in
 * configuration order. A source answers a list, not a single step: how many steps it stands for can
 * depend on what the session displays, so the answer is computed per session from the
 * {@link ViewContext} the wizard hands it.
 * </p>
 *
 * <p>
 * A source is stateless and shared by every session, like the element that holds it. How many steps
 * it stands for can change while the wizard is displayed; the channels that decide it are the ones
 * the source names in {@link #observedChannels(ViewContext)}.
 * </p>
 */
public interface WizardStepSource {

	/**
	 * Configuration for {@link WizardStepSource}.
	 */
	interface Config extends PolymorphicConfiguration<WizardStepSource> {
		// Marker interface.
	}

	/**
	 * The steps this source contributes, in the order the wizard walks them.
	 *
	 * @param context
	 *        The context of the wizard, resolving whatever the source decides by.
	 * @return The steps; empty for a source that contributes none in this session.
	 */
	List<WizardStep> steps(ViewContext context);

	/**
	 * The channels this source's contribution depends on.
	 *
	 * <p>
	 * The wizard follows them and asks its sources again whenever one takes a new value, so that a
	 * source computing its steps from a channel keeps the wizard in step with what that channel
	 * holds.
	 * </p>
	 *
	 * @param context
	 *        The context of the wizard, the same one {@link #steps(ViewContext)} is asked with.
	 * @return The channels; empty for a source whose steps are the same for the whole session.
	 */
	default List<ViewChannel> observedChannels(ViewContext context) {
		return List.of();
	}

	/**
	 * The content this source holds according to its configuration, as the wizard reports it from
	 * {@link com.top_logic.layout.view.UIElement#getChildGroups()}.
	 *
	 * @return The groups, in configuration order. Empty for a source whose content is not known
	 *         without a session.
	 */
	default List<ChildGroup> childGroups() {
		return List.of();
	}
}
