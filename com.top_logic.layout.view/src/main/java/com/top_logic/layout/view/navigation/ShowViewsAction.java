/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import com.top_logic.layout.view.command.WithValueArgument;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ArgumentsViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.navigation.DisplayTargetService.ShowConfig;

/**
 * {@link ViewAction} bringing several views into view in turn, each with the values its channels
 * receive.
 *
 * <p>
 * The views are displayed the way a display target's views are: the enclosing displays first, the
 * innermost one last, and each view is looked for within the view displayed before it, so a view
 * sitting on a tab of a frame the step before it pushed is revealed there.
 * </p>
 *
 * <pre>
 * &lt;show-views value="ticket"&gt;
 *   &lt;show view="projects/overview.view.xml"&gt;
 *     &lt;bind channel="project" expr="$ticket.container()"/&gt;
 *   &lt;/show&gt;
 *   &lt;show view="projects/ticket-detail.view.xml"&gt;
 *     &lt;bind channel="ticket"/&gt;
 *   &lt;/show&gt;
 * &lt;/show-views&gt;
 * </pre>
 *
 * <p>
 * Each binding is a script over the arguments of the chain, which it uses as variables without
 * declaring them (see {@link com.top_logic.layout.view.command.ActionScript}); a binding without an
 * expression receives the value the action processes itself.
 * </p>
 *
 * @implNote The action carries out a list of {@link ShowStep}s in one request; a single one is
 *           {@link ShowViewAction}.
 */
@InApp
@Label("Show views")
public class ShowViewsAction extends ArgumentsViewAction {

	/**
	 * Configuration for {@link ShowViewsAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ShowViewsAction>, WithValueArgument {

		/** Configuration tag of a {@link ShowViewsAction}. */
		String TAG_NAME = "show-views";

		/** Configuration name for {@link #getShows()}. */
		String SHOWS = "shows";

		/** Entry tag of {@link #getShows()}. */
		String SHOW = "show";

		@Override
		@ClassDefault(ShowViewsAction.class)
		Class<? extends ShowViewsAction> getImplementationClass();

		/**
		 * The views to display, the enclosing ones first, the innermost one last.
		 *
		 * @implNote Written directly as {@code <show>} children of the {@code <show-views>}.
		 */
		@Name(SHOWS)
		@EntryTag(SHOW)
		@DefaultContainer
		@Mandatory
		List<ShowConfig> getShows();
	}

	private final List<ShowStep> _shows;

	/**
	 * Creates a {@link ShowViewsAction} displaying the given views.
	 *
	 * @param shows
	 *        The views to display, outermost first. At least one.
	 */
	public ShowViewsAction(List<ShowStep> shows) {
		if (shows.isEmpty()) {
			throw new IllegalArgumentException("Displaying views needs a view to display.");
		}
		_shows = List.copyOf(shows);
	}

	/**
	 * Creates a new {@link ShowViewsAction} from configuration.
	 */
	@CalledByReflection
	public ShowViewsAction(InstantiationContext context, Config config) {
		this(ShowStep.fromConfigs(config.getShows(), ActionScope.of(context)));
	}

	/**
	 * Displays the views, their scripts evaluated over the given arguments; resumes the chain with
	 * the value the action processes.
	 */
	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		ObjectNavigation.show(context, _shows, arguments, new ResumeWith(continuation, arguments.value()));
	}

	/**
	 * Navigates to the views and writes their channels.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
