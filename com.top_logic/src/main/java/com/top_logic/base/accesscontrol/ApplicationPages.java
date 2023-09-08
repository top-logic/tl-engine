/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Global configuration of special pages in the application.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ApplicationPages {

	/**
	 * Simplified access to global {@link Config} options.
	 */
	public static ApplicationPages.Config getInstance() {
		return ApplicationConfig.getInstance().getConfig(ApplicationPages.Config.class);
	}

	/**
	 * Global pages configuration options.
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Configuration option that determines the suffix to the applications context path to which
		 * should be redirected after a successful login.
		 */
		@Name("startPage")
		@Mandatory
		String getStartPage();

		/**
		 * Configuration option that determines the page to deliver, if the server is buzzy.
		 */
		@Name("buzzyPage")
		@Mandatory
		String getBuzzyPage();

		/**
		 * Configuration option that determines the suffix to the applications context path to which
		 * should be redirected if missing cookie support is detected in the browser.
		 */
		@Name("noCookiePage")
		@Mandatory
		String getNoCookiePage();

		/**
		 * Configuration option that determines the suffix to the applications context path to which
		 * should be redirected for requesting authentication.
		 */
		@Name("loginPage")
		@Mandatory
		String getLoginPage();

		/**
		 * Configuration option that determines the suffix to the applications context path to which
		 * should be redirected in response to an final authentication failure.
		 */
		@Name("loginErrorPage")
		@Mandatory
		String getLoginErrorPage();

		/**
		 * Configuration option that determines the suffix to the applications context path to which
		 * should be redirected in response to an authentication failure.
		 */
		@Name("loginRetryPage")
		@Mandatory
		String getLoginRetryPage();

		/**
		 * Configuration option that determines the suffix to the applications context path to which
		 * should be redirected to trigger a logout.
		 */
		@Name("logoutPage")
		@Mandatory
		String getLogoutPage();

		/**
		 * Configuration option that determines the suffix to the applications context path to which
		 * should be redirected in response to an single-sign-on authentication failure.
		 */
		@Name("loginRetrySSO")
		@Mandatory
		String getLoginRetrySSOPage();

		/**
		 * The key under which the application local path to the layout servlet is configured.
		 */
		@Name("layout")
		@Mandatory
		String getLayoutServletPath();

		/**
		 * The key under which the application local path to the layout servlet is configured.
		 */
		@Name("login")
		@Mandatory
		String getLoginServletPath();

		/**
		 * The page that is displayed, if a user is required to change his password.
		 */
		@Mandatory
		@Name("changePassword")
		String getChangePasswordPage();
	}
}