/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.util.List;

import com.top_logic.base.discovery.AgentDiscoveryContribution;
import com.top_logic.base.discovery.AgentDiscoveryService;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.base.discovery.LlmsTxtSection;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.util.Resources;

/**
 * Names the {@link OAuthAuthorizationServer} in the <code>llms.txt</code> of the application
 * ({@link AgentDiscoveryService}), while it runs.
 */
public class OAuthDiscoveryContribution implements AgentDiscoveryContribution {

	/**
	 * Creates a {@link OAuthDiscoveryContribution}.
	 */
	@CalledByReflection
	public OAuthDiscoveryContribution() {
		// Nothing configurable: everything comes from the running OAuthAuthorizationServer.
	}

	@Override
	public boolean isActive() {
		return OAuthAuthorizationServer.getInstance() != null;
	}

	@Override
	public List<LlmsTxtSection> llmsTxtSections(DiscoveryContext context) {
		OAuthAuthorizationServer server = OAuthAuthorizationServer.getInstance();
		if (server == null) {
			return List.of();
		}
		return List.of(llmsTxtSection(server, context, Resources.getLogInstance()));
	}

	/**
	 * The section of the authorization server in <code>llms.txt</code>.
	 *
	 * @param server
	 *        The authorization server.
	 * @param context
	 *        The URLs of the application.
	 * @param resources
	 *        The language of the section.
	 */
	static LlmsTxtSection llmsTxtSection(OAuthAuthorizationServer server, DiscoveryContext context,
			Resources resources) {
		StringBuilder out = new StringBuilder();
		out.append(resources.getString(I18NConstants.LLMS_INTRO)).append("\n\n");
		out.append("- ")
			.append(resources.getString(
				I18NConstants.LLMS_METADATA__URL.fill(context.url(OAuthAuthorizationServer.METADATA_PATH))))
			.append('\n');
		out.append("- ")
			.append(resources.getString(I18NConstants.LLMS_ISSUER__URL.fill(context.applicationUrl())))
			.append('\n');
		OAuthAuthorizationServer.Config config = server.getConfig();
		if (config.getClientIdMetadataDocuments()) {
			out.append("- ").append(resources.getString(I18NConstants.LLMS_CLIENT_ID_METADATA_DOCUMENTS)).append('\n');
		}
		if (config.getDynamicClientRegistration()) {
			out.append("- ")
				.append(resources.getString(I18NConstants.LLMS_REGISTRATION__URL
					.fill(context.url(OAuthAuthorizationServer.REGISTRATION_PATH))))
				.append('\n');
		}
		return new LlmsTxtSection(resources.getString(I18NConstants.LLMS_TITLE), out.toString());
	}

}
