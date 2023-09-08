/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.internal;

import java.security.SecureRandom;

import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.layout.WindowContext;

/**
 * Identifier for a {@link WindowContext} in a {@link WindowRegistry}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class WindowId {

	private static final char RENDER_TOKEN_SEPARATOR = '-';

	private final String _windowName;

	private String _renderToken;

	private String _encodedForm;

	/**
	 * Creates a {@link WindowId} with random {@link #getWindowName()} and
	 * {@link #getRenderToken()}.
	 */
	public WindowId() {
		this(newWindowname());
	}

	private static String newWindowname() {
		SecureRandom random = SecureRandomService.getInstance().getRandom();
		String windowName = "w" + Long.toHexString(random.nextLong());
		return windowName;
	}

	/**
	 * Creates a {@link WindowId} with random {@link #getRenderToken()}.
	 * 
	 * @param windowName
	 *        See {@link #getWindowName()}.
	 */
	public WindowId(String windowName) {
		this(windowName, null);
	}

	private static String newRenderToken() {
		SecureRandom random = SecureRandomService.getInstance().getRandom();
		String newRenderToken = Long.toHexString(random.nextLong());
		return newRenderToken;
	}

	/**
	 * Creates a {@link WindowId}.
	 * 
	 * @param windowName
	 *        See {@link #getWindowName()}.
	 * @param renderToken
	 *        See {@link #getRenderToken()}.
	 */
	public WindowId(String windowName, String renderToken) {
		this(windowName, renderToken, encode(windowName, renderToken));
	}

	private static String encode(String windowName, String renderToken) {
		return windowName + (renderToken != null ? RENDER_TOKEN_SEPARATOR + renderToken : "");
	}

	private WindowId(String windowName, String renderToken, String encodedForm) {
		_windowName = windowName;
		_renderToken = renderToken;
		_encodedForm = encodedForm;
	}

	/**
	 * The (encoded) client-side window name, in which the subsession is homed.
	 */
	public String getWindowName() {
		return _windowName;
	}

	/**
	 * The token the client must provide to actually get the component tree rendered.
	 */
	public String getRenderToken() {
		return _renderToken;
	}

	/**
	 * The external for of this {@link WindowId} to be used as URL path element.
	 */
	public String getEncodedForm() {
		return _encodedForm;
	}

	/**
	 * Create a fresh random {@link #getRenderToken()}.
	 */
	public void updateRenderToken() {
		_renderToken = newRenderToken();
		_encodedForm = encode(_windowName, _renderToken);
	}

	/**
	 * Whether this {@link WindowId} has the given encoded form.
	 * 
	 * @see #getEncodedForm()
	 */
	public boolean matches(String encodedSubsessionId) {
		return _encodedForm.equals(encodedSubsessionId);
	}

	/**
	 * Creates a {@link WindowId} from its {@link #getEncodedForm()}.
	 */
	public static WindowId fromEncodedForm(String encodedSubsessionId) {
		int tokenSeparatorIndex = encodedSubsessionId.lastIndexOf(RENDER_TOKEN_SEPARATOR);
		if (tokenSeparatorIndex >= 0) {
			String windowName = encodedSubsessionId.substring(0, tokenSeparatorIndex);
			String renderToken = encodedSubsessionId.substring(tokenSeparatorIndex + 1);
			return new WindowId(windowName, renderToken, encodedSubsessionId);
		} else {
			return new WindowId(encodedSubsessionId, null, encodedSubsessionId);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof WindowId)) {
			return false;
		}

		return matches((WindowId) obj);
	}

	@Override
	public int hashCode() {
		return _encodedForm.hashCode();
	}

	/**
	 * Whether this {@link WindowId} equals the given one.
	 */
	public boolean matches(WindowId other) {
		return _encodedForm.equals(other._encodedForm);
	}

}
