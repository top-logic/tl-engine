/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.discovery;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.StringServices;
import com.top_logic.tl.ai.core.access.EndpointCredential;
import com.top_logic.util.Resources;

/**
 * Builds the MCP server card ({@link ServerCard}, SEP-2127) of the {@link MCPServerService}.
 *
 * <p>
 * The card is served at {@link #PATH} with the media type {@link #MEDIA_TYPE}, see
 * {@link ServerCardResource}. It names the server, its endpoint and the request header that carries
 * the credential, but never a credential itself, nor the tools or resources of the server.
 * </p>
 */
public final class McpServerCard {

	/**
	 * The media type of a server card.
	 */
	public static final String MEDIA_TYPE = "application/mcp-server-card+json";

	/**
	 * The {@link ServerCard#getSchema() schema} of the cards this class builds.
	 */
	public static final String SCHEMA_URL = "https://static.modelcontextprotocol.io/schemas/v1/server-card.schema.json";

	/**
	 * The path of the server card below the {@link MCPServerService#MCP_ENDPOINT MCP endpoint}, as
	 * the servlet mapping of the endpoint reports it as path info.
	 */
	public static final String PATH_INFO = "/server-card";

	/**
	 * The context-relative path of the server card.
	 */
	public static final String PATH = MCPServerService.MCP_ENDPOINT + PATH_INFO;

	/**
	 * The {@link ServerRemote#getType() type} of the MCP endpoint.
	 */
	public static final String STREAMABLE_HTTP = "streamable-http";

	/**
	 * The pattern a {@link ServerCard#getName() card name} matches.
	 */
	public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+/[a-zA-Z0-9._-]+$");

	/**
	 * The maximum length of a {@link ServerCard#getName() card name}.
	 */
	public static final int MAX_NAME_LENGTH = 200;

	/**
	 * The maximum length of the {@link ServerCard#getDescription() description} and the
	 * {@link ServerCard#getTitle() title} of a card.
	 */
	public static final int MAX_TEXT_LENGTH = 100;

	/**
	 * The last segment of the {@link ServerCard#getName() card name} of an application in the root
	 * context.
	 */
	static final String ROOT_CONTEXT_NAME = "mcp";

	/**
	 * The namespace of the {@link ServerCard#getName() card name} of an application whose URL has
	 * no host.
	 */
	static final String NO_HOST_NAMESPACE = "local";

	private static final Pattern NAMESPACE_INVALID = Pattern.compile("[^a-zA-Z0-9.-]");

	private static final Pattern NAME_INVALID = Pattern.compile("[^a-zA-Z0-9._-]");

	private McpServerCard() {
		// Static utility.
	}

	/**
	 * The server card of the given server.
	 *
	 * @param config
	 *        The configuration of the server.
	 * @param credentials
	 *        How a client presents the credentials the server accepts.
	 * @param context
	 *        The URLs of the application.
	 * @param resources
	 *        The language of the texts of the card.
	 */
	public static ServerCard create(MCPServerService.Config<?> config, List<EndpointCredential> credentials,
			DiscoveryContext context, Resources resources) {
		return ServerCard.create()
			.setSchema(SCHEMA_URL)
			.setName(name(config, context))
			.setVersion(config.getServerVersion())
			.setDescription(description(config, resources))
			.setTitle(truncate(StringServices.nonEmpty(config.getServerName()), MAX_TEXT_LENGTH))
			.setWebsiteUrl(context.applicationUrl())
			.addRemote(ServerRemote.create()
				.setType(STREAMABLE_HTTP)
				.setUrl(context.url(MCPServerService.MCP_ENDPOINT))
				.setHeaders(headers(credentials, resources)));
	}

	/**
	 * The {@link ServerCard#getName() card name} of the given server.
	 *
	 * @see MCPServerService.Config#getCardName()
	 */
	public static String name(MCPServerService.Config<?> config, DiscoveryContext context) {
		String configured = StringServices.nonEmpty(config.getCardName());
		String applicationUrl = context.applicationUrl();
		if (configured == null) {
			return name(defaultNamespace(applicationUrl), defaultLocalName(applicationUrl));
		}
		int slash = configured.indexOf('/');
		if (slash < 0) {
			return name(defaultNamespace(applicationUrl), configured);
		}
		return name(configured.substring(0, slash), configured.substring(slash + 1));
	}

	/**
	 * The part of a {@link #NAME_PATTERN valid card name} after the slash.
	 */
	public static String localName(String cardName) {
		return cardName.substring(cardName.indexOf('/') + 1);
	}

	/**
	 * The host name of the given URL, or {@link #NO_HOST_NAMESPACE} if it has none.
	 */
	public static String host(String url) {
		String host = parse(url).getHost();
		return StringServices.isEmpty(host) ? NO_HOST_NAMESPACE : host;
	}

	/**
	 * A valid card name from the given parts: invalid characters replaced by a hyphen, empty parts
	 * defaulted, and at most {@link #MAX_NAME_LENGTH} characters.
	 */
	static String name(String namespace, String localName) {
		String validNamespace = NAMESPACE_INVALID.matcher(namespace).replaceAll("-");
		if (validNamespace.isEmpty()) {
			validNamespace = NO_HOST_NAMESPACE;
		}
		String validName = NAME_INVALID.matcher(localName).replaceAll("-");
		if (validName.isEmpty()) {
			validName = ROOT_CONTEXT_NAME;
		}
		if (validNamespace.length() > MAX_NAME_LENGTH / 2) {
			validNamespace = validNamespace.substring(0, MAX_NAME_LENGTH / 2);
		}
		return truncate(validNamespace + '/' + validName, MAX_NAME_LENGTH);
	}

	/**
	 * The host name of the given application URL, its labels reversed.
	 */
	static String defaultNamespace(String applicationUrl) {
		String host = host(applicationUrl);
		String[] labels = host.split("\\.");
		StringBuilder result = new StringBuilder();
		for (int n = labels.length - 1; n >= 0; n--) {
			if (result.length() > 0) {
				result.append('.');
			}
			result.append(labels[n]);
		}
		return result.toString();
	}

	/**
	 * The last segment of the context path of the given application URL, {@link #ROOT_CONTEXT_NAME}
	 * for the root context.
	 */
	static String defaultLocalName(String applicationUrl) {
		String path = parse(applicationUrl).getPath();
		if (path == null) {
			return ROOT_CONTEXT_NAME;
		}
		while (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		String segment = path.substring(path.lastIndexOf('/') + 1);
		return segment.isEmpty() ? ROOT_CONTEXT_NAME : segment;
	}

	private static URI parse(String url) {
		try {
			return URI.create(url);
		} catch (IllegalArgumentException ex) {
			return URI.create("");
		}
	}

	private static String description(MCPServerService.Config<?> config, Resources resources) {
		String configured = StringServices.nonEmpty(config.getDescription());
		String description = configured != null ? configured.strip()
			: resources.getString(I18NConstants.CARD_DESCRIPTION__SERVER.fill(config.getServerName()));
		return truncate(description, MAX_TEXT_LENGTH);
	}

	/**
	 * The request headers that carry the given credentials.
	 *
	 * <p>
	 * Credentials sent in the same header share one entry. A header is required only if it carries
	 * every credential the server accepts; otherwise a client chooses one of the credentials.
	 * </p>
	 */
	private static List<RemoteHeader> headers(List<EndpointCredential> credentials, Resources resources) {
		Map<String, List<String>> descriptionsByHeader = new LinkedHashMap<>();
		boolean allInOneHeader = true;
		for (EndpointCredential credential : credentials) {
			if (!credential.isHeader()) {
				allInOneHeader = false;
				continue;
			}
			descriptionsByHeader.computeIfAbsent(credential.header(), x -> new ArrayList<>())
				.add(McpConnectionGuide.describe(credential, resources));
		}
		allInOneHeader &= descriptionsByHeader.size() == 1;

		List<RemoteHeader> result = new ArrayList<>();
		for (Map.Entry<String, List<String>> entry : descriptionsByHeader.entrySet()) {
			result.add(RemoteHeader.create()
				.setName(entry.getKey())
				.setDescription(String.join("; ", entry.getValue()))
				.setIsRequired(allInOneHeader)
				.setIsSecret(true));
		}
		return result;
	}

	private static String truncate(String text, int maxLength) {
		if (text == null || text.length() <= maxLength) {
			return text;
		}
		return text.substring(0, maxLength);
	}

}
