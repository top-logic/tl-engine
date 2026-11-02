/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.auth.JWTAuthentication;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.util.URLUtils;

/**
 * Nimbus {@link ClientAuthentication} sending a client assertion of any type together with the
 * client identifier (RFC 7521 §4.2).
 *
 * <p>
 * Nimbus' own {@link JWTAuthentication} expects an assertion whose issuer and subject are the
 * client identifier and omits the parameter <code>client_id</code>. This authentication also
 * passes an assertion issued by a third party, e.g. the service account token of a Kubernetes pod,
 * and always names the client, which some authorization servers require.
 * </p>
 */
public class ClientAssertion extends ClientAuthentication {

	/**
	 * Form parameter with the client identifier.
	 */
	public static final String CLIENT_ID = "client_id";

	/**
	 * Form parameter with the format of the assertion.
	 */
	public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";

	/**
	 * Form parameter with the assertion.
	 */
	public static final String CLIENT_ASSERTION = "client_assertion";

	private static final Set<String> PARAMETER_NAMES =
		Collections.unmodifiableSet(new LinkedHashSet<>(List.of(CLIENT_ID, CLIENT_ASSERTION_TYPE, CLIENT_ASSERTION)));

	private final String _assertionType;

	private final String _assertion;

	/**
	 * Creates a {@link ClientAssertion}.
	 *
	 * @param method
	 *        The authentication method, e.g. {@link ClientAuthenticationMethod#PRIVATE_KEY_JWT}.
	 * @param clientId
	 *        The identifier of the client.
	 * @param assertionType
	 *        The format of the assertion, e.g. {@link JWTAuthentication#CLIENT_ASSERTION_TYPE}.
	 * @param assertion
	 *        The assertion.
	 */
	public ClientAssertion(ClientAuthenticationMethod method, ClientID clientId, String assertionType,
			String assertion) {
		super(method, clientId);
		_assertionType = assertionType;
		_assertion = assertion;
	}

	@Override
	public Set<String> getFormParameterNames() {
		return PARAMETER_NAMES;
	}

	@Override
	public void applyTo(HTTPRequest httpRequest) {
		Map<String, List<String>> parameters;
		try {
			parameters = new LinkedHashMap<>(httpRequest.getBodyAsFormParameters());
		} catch (ParseException ex) {
			throw new SerializeException(ex.getMessage(), ex);
		}
		parameters.put(CLIENT_ID, Collections.singletonList(getClientID().getValue()));
		parameters.put(CLIENT_ASSERTION_TYPE, Collections.singletonList(_assertionType));
		parameters.put(CLIENT_ASSERTION, Collections.singletonList(_assertion));
		httpRequest.setBody(URLUtils.serializeParameters(parameters));
	}

}
