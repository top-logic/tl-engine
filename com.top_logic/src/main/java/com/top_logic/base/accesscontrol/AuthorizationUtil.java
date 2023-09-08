/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.version.Version;

/**
 * Utilities for authorisation of HTTP requests.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AuthorizationUtil {

	/** Name of the header containing the authorisation data. */
	public static final String AUTHORIZATION_HEADER_NAME = "Authorization";

	/**
	 * Prefix for the {@link #AUTHORIZATION_HEADER_NAME authorisation header} identifying
	 * <code>BasicAuth</code> authentication.
	 */
	static final String BASIC_AUTH_HEADER_PREFIX = "Basic ";

	static final String BEARER_HEADER_PREFIX = "Bearer ";

	private static final byte USERNAME_PASSWORD_SEPARATOR = (byte) ':';

	/** Name of the response header to request authentication. */
	public static final String AUTHENTICATE_RESPONSE_HEADER_NAME = "WWW-Authenticate";

	/** Character set in which the <code>BasicAuth</code> data are expected to be encoded. */
	public static final Charset UTF8 = StringServices.CHARSET_UTF_8;

	/**
	 * Fetches the login credentials from a <code>BasicAuth</code> header value.
	 * 
	 * @param basicAuthHeader
	 *        Basic authentication header value.
	 * @param credentialFactory
	 *        Function creating the actual login credentials from the user name and password.
	 * 
	 * @see #isBasicAuthentiation(String)
	 * @see #createBasicAuthAuthorizationHeader(String, String)
	 */
	public static <T> T retrieveBasicAuthCredentials(String basicAuthHeader,
			BiFunction<String, char[], T> credentialFactory) {
		String userPassCode = basicAuthHeader.substring(BASIC_AUTH_HEADER_PREFIX.length());
		byte[] userPass = Base64.decodeBase64(userPassCode);
		String user = getUser(userPass);
		char[] password = getPassword(userPass);
		// clear user and password
		Arrays.fill(userPass, (byte) 0);
		return credentialFactory.apply(user, password);
	}

	private static String getUser(byte[] userPass) {
		return new String(userPass, 0, getSeparatorIndex(userPass), AuthorizationUtil.UTF8);
	}

	private static char[] getPassword(byte[] userPass) {
		int firstPassCharIndex = getSeparatorIndex(userPass) + 1;
		ByteBuffer wrapped = ByteBuffer.wrap(userPass, firstPassCharIndex, userPass.length - firstPassCharIndex);
		CharBuffer decoded = AuthorizationUtil.UTF8.decode(wrapped);
		char[] result = decoded.array();
		if (decoded.limit() < result.length) {
			// Maybe not all slots in the array are actually used.
			result = Arrays.copyOf(result, decoded.limit());
		}
		return result;
	}

	private static int getSeparatorIndex(byte[] userPass) {
		int indexOf = ArrayUtil.indexOf(USERNAME_PASSWORD_SEPARATOR, userPass);
		if (indexOf == -1) {
			throw new RuntimeException("Expected user password has format <user>:<password>, but found no ':'.");
		}
		return indexOf;
	}

	/**
	 * Whether the {@link #getAuthorizationHeader(HttpServletRequest) authorisation header} is a
	 * <code>BasicAuth</code> authentication header
	 * 
	 * @param authorizationHeader
	 *        Authorisation header.
	 * 
	 * @see #getAuthorizationHeader(HttpServletRequest)
	 */
	public static boolean isBasicAuthentiation(String authorizationHeader) {
		return authorizationHeader.startsWith(BASIC_AUTH_HEADER_PREFIX);
	}

	/**
	 * Determines the {@link #AUTHORIZATION_HEADER_NAME authorisation header},
	 */
	public static String getAuthorizationHeader(HttpServletRequest request) {
		return request.getHeader(AUTHORIZATION_HEADER_NAME);
	}

	/**
	 * Whether an {@link #getAuthorizationHeader(HttpServletRequest) authorisation header} was sent.
	 */
	public static boolean authorizationSent(HttpServletRequest request) {
		return getAuthorizationHeader(request) != null;
	}

	/**
	 * Constructs the {@link #AUTHORIZATION_HEADER_NAME authorisation header} using
	 * <code>BasicAuth</code> authorisation.
	 * 
	 * @param user
	 *        User to authenticate.
	 * @param pwd
	 *        Password of the user to authenticate
	 * @return Value for {@link #AUTHORIZATION_HEADER_NAME authorisation header}.
	 * @see #retrieveBasicAuthCredentials(String, BiFunction)
	 */
	public static String createBasicAuthAuthorizationHeader(String user, String pwd) {
		byte[] userPass = (user + ":" + pwd).getBytes(UTF8);
		return BASIC_AUTH_HEADER_PREFIX + Base64.encodeBase64String(userPass);
	}

	/**
	 * Sets the response header that requests <code>BasicAuth</code> authentication.
	 */
	public static void setBasicAuthAuthenticationRequestHeader(HttpServletResponse response) {
		StringBuilder headerValue = new StringBuilder();
		headerValue.append(BASIC_AUTH_HEADER_PREFIX);
		appendRealm(headerValue);
		response.setHeader(AUTHENTICATE_RESPONSE_HEADER_NAME, headerValue.toString());
	}

	/**
	 * Sets the response header that requests <code>Bearer</code> authentication.
	 * 
	 * @param error
	 *        Error code like "invalid_request","invalid_token", or "insufficient_scope". May be
	 *        <code>null</code>.
	 * @param errorDescription
	 *        Additional description of the error. May be <code>null</code>.
	 */
	public static void setBearerAuthenticationRequestHeader(HttpServletResponse response, String error,
			String errorDescription) {
		StringBuilder headerValue = new StringBuilder();
		headerValue.append(BEARER_HEADER_PREFIX);
		appendRealm(headerValue);
		if (!StringServices.isEmpty(error)) {
			headerValue.append(",");
			headerValue.append("error=\"");
			headerValue.append(error);
			headerValue.append("\"");
		}
		if (!StringServices.isEmpty(errorDescription)) {
			headerValue.append(",");
			headerValue.append("error_description=\"");
			headerValue.append(errorDescription);
			headerValue.append("\"");
		}
		response.setHeader(AUTHENTICATE_RESPONSE_HEADER_NAME, headerValue.toString());
	}

	private static void appendRealm(StringBuilder headerValue) {
		headerValue.append("realm=\"");
		headerValue.append(Version.getApplicationName());
		headerValue.append("\"");
	}

}
