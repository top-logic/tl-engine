/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.servlet;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.jee.filter.SecurityFilter;
import org.pac4j.oidc.profile.OidcProfile;

import com.top_logic.base.accesscontrol.ExternalAuthenticationServlet;
import com.top_logic.base.accesscontrol.ExternalUserMapping;
import com.top_logic.base.accesscontrol.Login.InMaintenanceModeException;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.base.accesscontrol.Login.LoginFailedException;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;
import com.top_logic.security.auth.pac4j.config.UserNameExtractor;

/**
 * {@link ExternalAuthenticationServlet} processing a response from the pac4j
 * {@link SecurityFilter}.
 * 
 * <p>
 * The pac4j {@link SecurityFilter} and the {@link Pac4jAuthenticationServlet} are expected to be
 * configured for the same URI. The {@link SecurityFilter} authenticates the user and the
 * {@link Pac4jAuthenticationServlet} fetches the {@link CommonProfile} containing the data of the
 * authenticated user translating the external authentication to a login in a <i>TopLogic</i> application.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Pac4jAuthenticationServlet extends ExternalAuthenticationServlet {

	@Override
	protected LoginCredentials retrieveLoginCredentials(HttpServletRequest request, HttpServletResponse response)
			throws ForwardRequiredException, LoginDeniedException, LoginFailedException {
		Optional<UserProfile> profileHandle = userProfile(request, response);
		if (!profileHandle.isPresent()) {
			throw new LoginDeniedException("No user profile retrieved.");
		}

		UserProfile profile = profileHandle.get();

		String clientName = profile.getClientName();
		String userName = getUserName(profile, clientName);
		ExternalUserMapping userMapping = Pac4jConfigFactory.getInstance().getUserMapping(clientName);
		return LoginCredentials.fromUser(userMapping.findAccountForExternalName(userName));
	}

	private String getUserName(UserProfile profile, String clientName) {
		UserNameExtractor userNameExtractor = Pac4jConfigFactory.getInstance().getUserNameExtractor(clientName);
		return userNameExtractor.getUserName((CommonProfile) profile);
	}

	@Override
	protected void loginUser(Person person, HttpServletRequest request, HttpServletResponse response)
			throws InMaintenanceModeException {
		super.loginUser(person, request, response);
		UserProfile userProfile = userProfile(request, response).get();
		if (userProfile instanceof OidcProfile) {
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);
			installUserTokens(new Pac4jUserTokens(displayContext, (OidcProfile) userProfile));
		}
	}

	private Optional<UserProfile> userProfile(HttpServletRequest request, HttpServletResponse response) {
		Pac4jConfigFactory<?> configFactory = Pac4jConfigFactory.getInstance();
		Config config = configFactory.getPac4jConfig();

		WebContext context = new JEEContext(request, response);
		ProfileManager manager = config.getProfileManagerFactory().apply(context, new JEESessionStore());
		manager.setConfig(config);
		return manager.getProfile();
	}

}
