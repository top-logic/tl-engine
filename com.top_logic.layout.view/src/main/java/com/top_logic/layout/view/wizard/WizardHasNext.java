/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that shows a command only while a step follows the one the
 * enclosing wizard displays.
 *
 * <p>
 * Guards the command that moves on, so that it is offered on every step but the last, where moving
 * on would do nothing. Outside a wizard the command stays hidden.
 * </p>
 */
public class WizardHasNext implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

	/**
	 * Configuration for {@link WizardHasNext}.
	 */
	@TagName("wizard-has-next")
	public interface Config extends ViewExecutabilityRule.Config {

		@Override
		@ClassDefault(WizardHasNext.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();
	}

	private WizardScope _scope;

	/**
	 * Creates a {@link WizardHasNext} rule from configuration.
	 */
	@CalledByReflection
	public WizardHasNext(InstantiationContext context, Config config) {
		// The guarded wizard is resolved from the command's context.
	}

	/**
	 * Creates a {@link WizardHasNext} rule for the given wizard.
	 */
	public WizardHasNext(WizardScope scope) {
		_scope = scope;
	}

	@Override
	public void bind(ViewContext context) {
		_scope = context.getScope(WizardScope.class);
	}

	/**
	 * Follows the step the wizard displays: moving is what turns the command off and on again.
	 */
	@Override
	public Runnable observe(Runnable revalidate) {
		if (_scope == null) {
			return () -> {
				// No wizard to follow.
			};
		}
		ViewChannel stepChannel = _scope.stepChannel();
		ChannelListener listener = (sender, oldValue, newValue) -> revalidate.run();
		stepChannel.addListener(listener);
		return () -> stepChannel.removeListener(listener);
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return _scope != null && _scope.hasNext() ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}
}
