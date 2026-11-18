/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedEnum;
import com.top_logic.basic.config.ExternallyNamed;

/**
 * Controls how the LLM selects tools during a request.
 */
public enum ToolChoice implements ExternallyNamed {

	/** The LLM decides whether to call a tool (default). */
	AUTO("auto"),

	/** The LLM must call at least one tool. */
	REQUIRED("required"),

	/** The LLM must not call any tool. */
	NONE("none"),

	;

	private final String _externalName;

	ToolChoice(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * The tool choice of the given configured value.
	 *
	 * @param value
	 *        The name of a tool choice, e.g. {@code REQUIRED} or {@code required}; case does not
	 *        matter.
	 * @return The tool choice, <code>null</code> for a blank or unknown value, which leaves the
	 *         choice to the provider.
	 */
	public static ToolChoice parse(String value) {
		if (value == null) {
			return null;
		}
		String name = value.trim();
		for (ToolChoice choice : values()) {
			if (choice.name().equalsIgnoreCase(name)) {
				return choice;
			}
		}
		return null;
	}

	/**
	 * Format of a comma-separated list of {@link ToolChoice}s in a configuration, e.g.
	 * {@code auto,required,none}.
	 */
	public static class ListFormat extends CommaSeparatedEnum<ToolChoice> {

		/** Sole instance of {@link ListFormat}. */
		public static final ListFormat INSTANCE = new ListFormat();

		private ListFormat() {
			super(ToolChoice.class);
		}
	}

	/**
	 * The request as an endpoint receives it that accepts only the given tool choices.
	 *
	 * <p>
	 * A request without a tool choice, or with one the endpoint accepts, is returned unchanged. A
	 * request that forbids tool calls ({@link #NONE}) is sent with {@link #AUTO} instead, if the
	 * endpoint accepts that: the tools stay in the request, and the model decides. Any other choice
	 * the endpoint does not accept is refused before the request is sent - a forced tool call
	 * ({@link #REQUIRED}) cannot be approximated.
	 * </p>
	 *
	 * @param request
	 *        The request to send.
	 * @param supported
	 *        The tool choices the endpoint accepts.
	 * @param endpoint
	 *        Name of the endpoint for the error message.
	 * @throws ProviderException
	 *         of kind {@link ProviderException.Kind#INVALID_REQUEST} if the request's tool choice
	 *         cannot be sent to the endpoint.
	 */
	public static ProviderRequest restrict(ProviderRequest request, Set<ToolChoice> supported, String endpoint)
			throws ProviderException {
		ToolChoice choice = request.toolChoice();
		if (choice == null || supported.contains(choice)) {
			return request;
		}
		if (choice == NONE && supported.contains(AUTO)) {
			return request.withToolChoice(AUTO);
		}
		throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 0,
			"The endpoint '" + endpoint + "' does not accept the tool choice '"
				+ ListFormat.INSTANCE.getSpecification(List.of(choice)) + "'; it accepts only '"
				+ ListFormat.INSTANCE.getSpecification(List.copyOf(supported)) + "'.");
	}
}
