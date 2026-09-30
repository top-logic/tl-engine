/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.navigation.DisplayTargetService.ShowConfig;

/**
 * {@link ViewAction} bringing one view into view and writing the values its channels receive.
 *
 * <p>
 * The view is named by its file, so a command leads to a place of the application without a
 * business object being involved:
 * </p>
 *
 * <pre>
 * &lt;show-view view="tickets.view.xml"&gt;
 *   &lt;bind channel="activeFilter" expr="term -&gt; 'all'"/&gt;
 *   &lt;bind channel="searchTerm" expr="term -&gt; $term"/&gt;
 * &lt;/show-view&gt;
 * </pre>
 *
 * <p>
 * The view is reached wherever it is displayed: the containers on the way to it are opened, or it
 * is drilled down to as a frame of the surrounding stack, or - with {@code dialog} - opened as a
 * dialog. Each binding is a function of the chain's current value; a binding without an expression
 * receives that value itself. The chain continues with the value it had, so what a command does
 * after displaying the view is unaffected.
 * </p>
 *
 * @implNote The action carries out one {@link ShowStep}; a list of them is
 *           {@link ShowViewsAction}.
 */
@InApp
@Label("Show view")
public class ShowViewAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link ShowViewAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ShowViewAction>, ShowConfig {

		/** Configuration tag of a {@link ShowViewAction}. */
		String TAG_NAME = "show-view";

		@Override
		@ClassDefault(ShowViewAction.class)
		Class<? extends ShowViewAction> getImplementationClass();
	}

	private final List<ShowStep> _shows;

	/**
	 * Creates a {@link ShowViewAction} displaying the given view.
	 *
	 * @param show
	 *        The view to display, with the values its channels receive.
	 */
	public ShowViewAction(ShowStep show) {
		_shows = List.of(show);
	}

	/**
	 * Creates a new {@link ShowViewAction} from configuration.
	 */
	@CalledByReflection
	public ShowViewAction(InstantiationContext context, Config config) {
		this(ShowStep.fromConfig(config));
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		ObjectNavigation.show(context, _shows, input, new ResumeWith(continuation, input));
	}
}
