/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.view.StartPage;
import com.top_logic.layout.view.ViewMessages;

/**
 * Remembers the page the user is looking at as the one they start on, or forgets it again when
 * they are already there.
 *
 * <p>
 * One entry serves both directions, because the question a user asks of it is the same either way -
 * whether this page is where they begin. Invoked on the page that is already the start page it
 * clears the choice, so nothing has to offer a second entry that is meaningless most of the time.
 * </p>
 *
 * @see StartPage
 */
@InApp
public class RememberStartPageAction implements ViewAction {

	/**
	 * Configuration for {@link RememberStartPageAction}.
	 */
	@TagName("remember-start-page")
	public interface Config extends PolymorphicConfiguration<RememberStartPageAction> {

		@Override
		@ClassDefault(RememberStartPageAction.class)
		Class<? extends RememberStartPageAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link RememberStartPageAction} from configuration.
	 */
	@CalledByReflection
	public RememberStartPageAction(InstantiationContext context, Config config) {
		// No configuration needed: the page in view is the one remembered.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		RouteManager routes = context.getRouteManager();
		String current = routes == null ? null : routes.currentUrl();
		if (StringServices.isEmpty(current)) {
			// Nothing identifies this page, so there is nothing to come back to.
			ViewMessages.info(context, I18NConstants.START_PAGE_NOT_ADDRESSABLE);
			return input;
		}

		if (current.equals(StartPage.get())) {
			StartPage.set(null);
			ViewMessages.info(context, I18NConstants.START_PAGE_FORGOTTEN);
		} else {
			StartPage.set(current);
			ViewMessages.info(context, I18NConstants.START_PAGE_REMEMBERED);
		}
		return input;
	}

}
