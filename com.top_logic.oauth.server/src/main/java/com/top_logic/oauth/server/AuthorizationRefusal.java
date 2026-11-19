/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.net.URI;
import java.net.URISyntaxException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * Why the authorization endpoint refuses a request without sending the browser back to the
 * client.
 *
 * <p>
 * The authorization endpoint sends the browser to the
 * refusal page with the name of the refusal in the parameter
 * {@value OAuthAuthorizationServer#REASON_PARAMETER} and the value the message names in the
 * parameter {@value OAuthAuthorizationServer#DETAIL_PARAMETER}. That value is either the
 * application's own address or a value the client sent, never a secret.
 * </p>
 *
 * @see OAuthAuthorizationServer.Config#getRefusalPage()
 */
public enum AuthorizationRefusal {

	/**
	 * The client reaches the application over plain HTTP on a host other than the loopback
	 * interface.
	 *
	 * <p>
	 * Detail: the address of the application.
	 * </p>
	 *
	 * @see OAuthAuthorizationServer#isSecure(com.top_logic.base.discovery.DiscoveryContext)
	 */
	INSECURE_CONNECTION(I18NConstants.ERROR_AUTHORIZATION_WITHOUT_TLS__URL),

	/**
	 * The client is not known or its metadata are not acceptable.
	 *
	 * <p>
	 * Detail: the client ID.
	 * </p>
	 */
	UNKNOWN_CLIENT(I18NConstants.REFUSAL_UNKNOWN_CLIENT__CLIENT),

	/**
	 * The redirect URI is not registered for the client.
	 *
	 * <p>
	 * Detail: the redirect URI.
	 * </p>
	 */
	UNREGISTERED_REDIRECT_URI(I18NConstants.REFUSAL_UNREGISTERED_REDIRECT_URI__URI),

	/**
	 * The request cannot be parsed, or lacks client ID or redirect URI.
	 *
	 * <p>
	 * No detail.
	 * </p>
	 */
	MALFORMED_REQUEST(null);

	/** The host name of the loopback interface used in the example address of a hint. */
	private static final String LOCALHOST = "localhost";

	private final ResKey1 _message;

	private AuthorizationRefusal(ResKey1 message) {
		_message = message;
	}

	/**
	 * The message telling the person why the request is refused.
	 *
	 * @param detail
	 *        The value the message names, see the documentation of the constants. If it is missing
	 *        although the message needs one, the message is a general one.
	 */
	public ResKey message(String detail) {
		if (this == MALFORMED_REQUEST) {
			return I18NConstants.REFUSAL_MALFORMED_REQUEST;
		}
		if (detail == null || detail.isEmpty()) {
			return I18NConstants.REFUSAL_UNKNOWN_REASON;
		}
		return _message.fill(detail);
	}

	/**
	 * What the person can do about the refusal.
	 *
	 * @param detail
	 *        See {@link #message(String)}.
	 */
	public ResKey hint(String detail) {
		if (this == INSECURE_CONNECTION) {
			String localUrl = localUrl(detail);
			return localUrl == null ? I18NConstants.REFUSAL_HINT_INSECURE_CONNECTION
				: I18NConstants.REFUSAL_HINT_INSECURE_CONNECTION__LOCAL_URL.fill(localUrl);
		}
		return I18NConstants.REFUSAL_HINT_RETRY;
	}

	/**
	 * The refusal with the given {@link #name()}.
	 *
	 * @return The refusal, or <code>null</code> if there is none with that name.
	 */
	public static AuthorizationRefusal fromName(String name) {
		if (name == null) {
			return null;
		}
		for (AuthorizationRefusal refusal : values()) {
			if (refusal.name().equals(name)) {
				return refusal;
			}
		}
		return null;
	}

	/**
	 * The given address of the application with the host replaced by {@link #LOCALHOST}.
	 *
	 * @return The address on the loopback interface, or <code>null</code> if the given one is no
	 *         absolute address with a host.
	 */
	private static String localUrl(String applicationUrl) {
		if (applicationUrl == null) {
			return null;
		}
		try {
			URI url = new URI(applicationUrl);
			if (url.getScheme() == null || url.getHost() == null) {
				return null;
			}
			return new URI(url.getScheme(), null, LOCALHOST, url.getPort(), url.getPath(), null, null).toString();
		} catch (URISyntaxException ex) {
			return null;
		}
	}

}
