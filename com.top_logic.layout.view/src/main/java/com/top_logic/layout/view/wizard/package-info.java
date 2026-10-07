/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Multi-step flows for the view layer.
 *
 * <p>
 * A {@link com.top_logic.layout.view.wizard.WizardElement &lt;wizard&gt;} walks a sequence of
 * {@link com.top_logic.layout.view.wizard.WizardStep steps} and displays one of them at a time,
 * with an indicator saying where in the sequence the user is. The sequence is the concatenation of
 * what the wizard's {@link com.top_logic.layout.view.wizard.WizardStepSource sources} contribute; a
 * {@link com.top_logic.layout.view.wizard.StaticStepSource &lt;step&gt;} stands for the one step it
 * is written as, a {@link com.top_logic.layout.view.wizard.DynamicStepsSource &lt;dynamic-steps&gt;}
 * for one step per element of a list a channel holds. The two mix, so a flow can open with a
 * written-out step, continue over as many elements as the channel holds, and close with another
 * written-out step. A source names the channels its contribution depends on, and the wizard expands
 * the sequence anew whenever one of them takes a new value.
 * </p>
 *
 * <p>
 * The key of the step displayed lives on a
 * {@link com.top_logic.layout.view.channel.ViewChannel}, and that channel is the single source of
 * truth: every move is a write to it, and a value it does not name a step with displays the first
 * step. Binding the channel into the address of the page therefore makes a step a deep link,
 * through the same {@code <param-bindings>} any other channel uses.
 * </p>
 *
 * <p>
 * The moves are the commands
 * {@link com.top_logic.layout.view.wizard.WizardNextCommand &lt;wizard-next&gt;},
 * {@link com.top_logic.layout.view.wizard.WizardBackCommand &lt;wizard-back&gt;} and
 * {@link com.top_logic.layout.view.wizard.WizardGotoCommand &lt;wizard-goto&gt;}, each also
 * available as a {@link com.top_logic.layout.view.command.ViewAction} of the same tag so that
 * moving on can be the last step of a longer command chain. They find the wizard through the
 * {@link com.top_logic.layout.view.wizard.WizardScope} every step's content sees, which is also
 * what the rules {@link com.top_logic.layout.view.wizard.WizardHasNext &lt;wizard-has-next&gt;} and
 * {@link com.top_logic.layout.view.wizard.WizardHasBack &lt;wizard-has-back&gt;} ask, so that a
 * Back button is absent on the first step and a Next button on the last.
 * </p>
 *
 * <p>
 * The wizard renders no navigation buttons of its own: which buttons a flow offers, and where they
 * sit, is composed in the view out of those commands.
 * </p>
 */
package com.top_logic.layout.view.wizard;
