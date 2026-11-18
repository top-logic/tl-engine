/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import java.io.IOError;
import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.security.selfservice.I18NConstants;
import com.top_logic.util.error.TopLogicException;

/**
 * The absolute address of a page of the application, as a mail leads to it.
 *
 * <p>
 * An address consists of the public address of the application ({@link AliasManager#HOST},
 * variable <code>tl_host</code>, or without one the address of the current request), its context
 * path ({@link AliasManager#APP_CONTEXT}) and the path of the page below the context path, e.g.
 * <code>https://app.example.com/app/servlet/LayoutServlet</code>. With a configured public
 * address, an address does not depend on a current request and can be built in any context, e.g.
 * from a scheduled script.
 * </p>
 *
 * @see InvitationLink
 */
public class ApplicationAddress {

	/**
	 * The absolute URL of the page with the given path.
	 *
	 * @param path
	 *        The path of the page below the context path of the application, starting with
	 *        <code>/</code>, optionally followed by request parameters.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_NO_PUBLIC_ADDRESS} if the public address of the
	 *         application is not configured and there is no current request.
	 */
	public static String url(String path) {
		return publicAddress() + contextPath() + path;
	}

	/**
	 * The absolute URL of the {@link ApplicationPages.Config#getLoginPage() login page} of the
	 * application.
	 *
	 * @see #url(String)
	 */
	public static String loginUrl() {
		return url(ApplicationPages.getInstance().getLoginPage());
	}

	/**
	 * The public address of the application (scheme, host and optional port) without a trailing
	 * slash.
	 *
	 * <p>
	 * The address is the configured public address of the application ({@link AliasManager#HOST}).
	 * Without one, it is taken from the current request, if the address is built while a request
	 * is processed, e.g. in a command of the user interface.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_NO_PUBLIC_ADDRESS} if the public address of the
	 *         application is not configured and there is no current request.
	 */
	public static String publicAddress() {
		String host = StringServices.nonNull(AliasManager.getInstance().getAlias(AliasManager.HOST)).trim();
		if (host.isEmpty()) {
			HttpServletRequest request = currentRequest();
			if (request == null) {
				throw new TopLogicException(I18NConstants.ERROR_NO_PUBLIC_ADDRESS);
			}
			StringBuilder requestHost = new StringBuilder();
			try {
				LayoutUtils.appendHostURL(request, requestHost);
			} catch (IOException ex) {
				// A StringBuilder does not throw.
				throw new IOError(ex);
			}
			host = requestHost.toString();
		}
		while (host.endsWith("/")) {
			host = host.substring(0, host.length() - 1);
		}
		return host;
	}

	/**
	 * The context path of the application, empty for the root context.
	 */
	public static String contextPath() {
		return StringServices.nonNull(AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT));
	}

	private static HttpServletRequest currentRequest() {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			return null;
		}
		return interaction.asRequest();
	}

}
