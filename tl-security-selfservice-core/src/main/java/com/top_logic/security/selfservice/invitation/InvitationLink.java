/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import java.io.IOError;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.security.selfservice.I18NConstants;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.util.error.TopLogicException;

/**
 * The link of an {@link Invitation}, the page of the application where the invited person accepts
 * it.
 *
 * <p>
 * The link is the public address of the application ({@link AliasManager#HOST}, variable
 * <code>tl_host</code>, or without one the address of the current request), its context path ({@link AliasManager#APP_CONTEXT}), the
 * {@link Config#getPage() page} and the {@link Invitation#getId() token} of the invitation in the
 * request parameter {@link #INVITATION_PARAMETER}, e.g.
 * <code>https://app.example.com/app/servlet/LayoutServlet?invitation=0123abcd-...</code>.
 * With a configured public address, the link does not depend on a current request and can be built
 * in any context, e.g. from a scheduled script.
 * </p>
 */
public class InvitationLink<C extends InvitationLink.Config<?>> extends AbstractConfiguredInstance<C> {

	/**
	 * Request parameter of the link holding the {@link Invitation#getId() token} of the
	 * {@link Invitation}.
	 */
	public static final String INVITATION_PARAMETER = "invitation";

	/**
	 * Configuration options for {@link InvitationLink}.
	 */
	public interface Config<I extends InvitationLink<?>> extends PolymorphicConfiguration<I> {

		/** Configuration name for {@link #getPage()}. */
		String PAGE = "page";

		/**
		 * The page the link leads to, a path below the context path of the application starting
		 * with <code>/</code>, e.g. <code>/servlet/LayoutServlet</code>.
		 *
		 * <p>
		 * The page receives the token of the invitation in the request parameter
		 * {@link InvitationLink#INVITATION_PARAMETER}. Without a value, the link leads to the
		 * {@link ApplicationPages.Config#getLayoutServletPath() layout servlet} of the classic user
		 * interface, whose login accepts the invitation.
		 * </p>
		 */
		@Name(PAGE)
		@Nullable
		String getPage();

	}

	/**
	 * Creates a {@link InvitationLink} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public InvitationLink(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * The absolute URL of the link for the given token.
	 *
	 * @param token
	 *        The {@link Invitation#getId() token} of the invitation.
	 *
	 * @throws TopLogicException
	 *         If the public address of the application is not configured.
	 */
	public String url(String token) {
		return publicAddress() + contextPath() + path(token);
	}

	/**
	 * The link for the given token relative to the context path of the application, starting with
	 * <code>/</code>.
	 *
	 * @param token
	 *        The {@link Invitation#getId() token} of the invitation.
	 */
	public String path(String token) {
		return page() + "?" + INVITATION_PARAMETER + "=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
	}

	/**
	 * The page the link leads to, a path below the context path of the application.
	 *
	 * @see Config#getPage()
	 */
	protected String page() {
		String page = getConfig().getPage();
		if (StringServices.isEmpty(page)) {
			return ApplicationPages.getInstance().getLayoutServletPath();
		}
		return page;
	}

	/**
	 * The public address of the application (scheme, host and optional port) without a trailing
	 * slash.
	 *
	 * <p>
	 * The address is the configured public address of the application ({@link AliasManager#HOST}).
	 * Without one, it is taken from the current request, if the link is built while a request is
	 * processed, e.g. in a command of the user interface.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the public address of the application is not configured and there is no current
	 *         request.
	 */
	protected String publicAddress() {
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

	private static HttpServletRequest currentRequest() {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			return null;
		}
		return interaction.asRequest();
	}

	private static String contextPath() {
		return StringServices.nonNull(AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT));
	}

}
