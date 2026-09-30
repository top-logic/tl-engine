/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ViewContext;

/**
 * One step of a {@link WizardElement &lt;wizard&gt;}, as the wizard displays it in a session.
 *
 * <p>
 * A step is what a {@link WizardStepSource} contributes: the key naming it, how it is labelled in
 * the step indicator, and how its content is built. The key is the value the wizard's
 * {@link WizardElement.Config#getCurrentStep() step channel} holds while this step is the one
 * displayed, and the value {@link WizardScope#goTo(Object)} takes.
 * </p>
 *
 * @param key
 *        Identity of the step, unique within the wizard. Any object whose {@code equals} tells the
 *        step apart from its siblings; a step declared in the view is keyed by its {@code id}.
 * @param label
 *        Name of the step in the step indicator, {@code null} for a step the indicator names by its
 *        position alone.
 * @param icon
 *        Encoded icon shown beside the label (e.g. {@code css:fa-solid fa-user}), {@code null} for
 *        a step without one.
 * @param content
 *        Builds the content of the step in the child context the wizard hands it. Called when the
 *        step is displayed, and again whenever it is displayed after having been left.
 * @param autoAdvanceMillis
 *        How long the step is displayed before the wizard moves on by itself, {@code null} for a
 *        step the user leaves. The time runs while the flow leads through the step: a step the user
 *        came back to waits for them, however it is configured.
 */
public record WizardStep(Object key, ResKey label, String icon, Function<ViewContext, ReactControl> content,
		Long autoAdvanceMillis) {
	// Pure data.
}
