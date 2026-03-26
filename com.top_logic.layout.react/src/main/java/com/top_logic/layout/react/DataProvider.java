/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.servlet.ReactServlet;

/**
 * Interface for React controls that serve binary data to the client.
 *
 * <p>
 * Controls implementing this interface can provide binary data (e.g. audio, images) for download by
 * the React client via the {@code /react-api/data} endpoint. The {@link ReactServlet} dispatches
 * incoming GET requests to the appropriate control based on the {@code controlId} parameter.
 * </p>
 */
public interface DataProvider {

	/**
	 * Returns the binary data for the given key, or {@code null} if no data is available.
	 *
	 * @param key
	 *        Identifies the requested data item. May be {@code null} for controls that serve a
	 *        single item.
	 * @return The binary data, or {@code null}.
	 */
	BinaryData getDownloadData(String key);

}
