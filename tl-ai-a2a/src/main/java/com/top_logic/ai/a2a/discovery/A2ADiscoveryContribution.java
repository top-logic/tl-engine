/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.discovery;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.A2AService.ExposedAgentConfig;
import com.top_logic.ai.a2a.A2AServlet;
import com.top_logic.base.discovery.AgentDiscoveryContribution;
import com.top_logic.base.discovery.AgentDiscoveryService;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.base.discovery.LlmsTxtSection;
import com.top_logic.base.discovery.catalog.CatalogEntry;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.tl.ai.core.access.EndpointCredential;
import com.top_logic.util.Resources;

/**
 * Describes the agents the {@link A2AService} exposes in the self-description of the application
 * ({@link AgentDiscoveryService}), while the service runs and exposes at least one agent.
 *
 * <ul>
 * <li>The AI Catalog lists the agent card of every exposed agent with the media type
 * {@link #MEDIA_TYPE}, identified as <code>urn:air:&lt;host&gt;:a2a:&lt;path&gt;</code>
 * ({@link DiscoveryContext#catalogIdentifier(String, String)}).</li>
 * <li><code>llms.txt</code> gets one section listing the agents with their cards and endpoints and
 * the credentials the endpoints accept, see {@link #llmsTxtSection}.</li>
 * </ul>
 */
public class A2ADiscoveryContribution implements AgentDiscoveryContribution {

	/**
	 * The media type of an A2A agent card.
	 */
	public static final String MEDIA_TYPE = "application/a2a-agent-card+json";

	/**
	 * The namespace of the catalog {@link CatalogEntry#getIdentifier() identifier} of an agent card.
	 */
	public static final String IDENTIFIER_NAMESPACE = "a2a";

	/**
	 * Creates a {@link A2ADiscoveryContribution}.
	 */
	@CalledByReflection
	public A2ADiscoveryContribution() {
		// Nothing configurable: everything comes from the running A2AService.
	}

	@Override
	public boolean isActive() {
		A2AService service = A2AService.getInstance();
		return service != null && !service.getConfig().getAgents().isEmpty();
	}

	@Override
	public List<LlmsTxtSection> llmsTxtSections(DiscoveryContext context) {
		A2AService service = A2AService.getInstance();
		if (service == null) {
			return List.of();
		}
		return llmsTxtSections(service.getConfig().getAgents(), service.getCredentials(), context,
			Resources.getLogInstance());
	}

	@Override
	public List<CatalogEntry> catalogEntries(DiscoveryContext context) {
		A2AService service = A2AService.getInstance();
		if (service == null) {
			return List.of();
		}
		return catalogEntries(service.getConfig().getAgents(), context);
	}

	/**
	 * The catalog entries of the cards of the given agents.
	 */
	static List<CatalogEntry> catalogEntries(List<ExposedAgentConfig> agents, DiscoveryContext context) {
		List<CatalogEntry> result = new ArrayList<>(agents.size());
		for (ExposedAgentConfig agent : agents) {
			result.add(CatalogEntry.create()
				.setIdentifier(context.catalogIdentifier(IDENTIFIER_NAMESPACE, agent.getPath()))
				.setType(MEDIA_TYPE)
				.setUrl(context.url(A2AServlet.agentCardPath(agent.getPath())))
				.setDisplayName(A2AService.displayName(agent))
				.setDescription(description(agent))
				.setVersion(A2AService.AGENT_VERSION));
		}
		return result;
	}

	/**
	 * The section of the given agents in <code>llms.txt</code>: none if there is no agent.
	 *
	 * @param agents
	 *        The exposed agents.
	 * @param credentials
	 *        How a client presents the credentials the endpoints accept.
	 * @param context
	 *        The URLs of the application.
	 * @param resources
	 *        The language of the section.
	 */
	static List<LlmsTxtSection> llmsTxtSections(List<ExposedAgentConfig> agents,
			List<EndpointCredential> credentials, DiscoveryContext context, Resources resources) {
		if (agents.isEmpty()) {
			return List.of();
		}
		return List.of(llmsTxtSection(agents, credentials, context, resources));
	}

	/**
	 * The section of the given agents in <code>llms.txt</code>.
	 *
	 * @see #llmsTxtSections(List, List, DiscoveryContext, Resources)
	 */
	static LlmsTxtSection llmsTxtSection(List<ExposedAgentConfig> agents, List<EndpointCredential> credentials,
			DiscoveryContext context, Resources resources) {
		StringBuilder out = new StringBuilder();
		out.append(resources.getString(I18NConstants.LLMS_INTRO)).append("\n\n");
		for (ExposedAgentConfig agent : agents) {
			String name = A2AService.displayName(agent);
			String card = context.url(A2AServlet.agentCardPath(agent.getPath()));
			String endpoint = context.url(A2AServlet.agentPath(agent.getPath()));
			String description = description(agent);
			out.append("- ").append(resources.getString(description == null
				? I18NConstants.LLMS_AGENT__NAME_CARD_ENDPOINT.fill(name, card, endpoint)
				: I18NConstants.LLMS_AGENT__NAME_CARD_ENDPOINT_DESCRIPTION.fill(name, card, endpoint, description)));
			out.append('\n');
		}
		out.append('\n');
		if (credentials.isEmpty()) {
			out.append(resources.getString(I18NConstants.NO_CREDENTIALS)).append('\n');
		} else {
			out.append(resources.getString(I18NConstants.CREDENTIALS_INTRO)).append('\n');
			for (EndpointCredential credential : credentials) {
				out.append("- ").append(credential.describeWithOrigin(context, resources)).append('\n');
			}
		}
		return new LlmsTxtSection(resources.getString(I18NConstants.LLMS_TITLE), out.toString());
	}

	/**
	 * The configured description of the given agent, or <code>null</code>.
	 */
	private static String description(ExposedAgentConfig agent) {
		String description = agent.getDescription();
		return description == null || description.isBlank() ? null : description.strip();
	}

}
