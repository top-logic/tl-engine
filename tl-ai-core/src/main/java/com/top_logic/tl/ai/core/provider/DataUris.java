/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

/**
 * Helpers for {@code data:} URIs.
 */
public final class DataUris {

	private DataUris() {
		// Static utility.
	}

	/**
	 * Extracts the payload of a {@code data:} URI: everything after the first comma (e.g. the
	 * base64 body of {@code data:image/png;base64,<payload>}). Returns the input unchanged if it
	 * contains no comma.
	 */
	public static String base64Payload(String dataUri) {
		int commaIdx = dataUri.indexOf(',');
		return commaIdx >= 0 ? dataUri.substring(commaIdx + 1) : dataUri;
	}
}
