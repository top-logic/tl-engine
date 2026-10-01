/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.net.URI;

/**
 * Optional capability of a {@link BinaryData} whose content the browser can fetch directly from
 * the storage holding it, without passing through the application server.
 *
 * <p>
 * A server delivering binary content to a browser asks the value for a direct download URL and
 * answers with a redirect to it. If the value has no such URL, the server streams the content
 * itself. Only content kept in an external storage that can issue time-limited URLs (e.g.
 * presigned URLs of an S3 object storage) offers direct downloads; content kept in the database or
 * in memory never does.
 * </p>
 *
 * @see #getDirectDownloadUrl(BinaryData)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DirectDownload {

	/**
	 * A URL from which the browser fetches the content of this value directly.
	 *
	 * <p>
	 * The URL delivers the content with the content type and name of this value. It is valid for a
	 * limited time only and must therefore be requested anew for each download.
	 * </p>
	 *
	 * @return The download URL, or <code>null</code> if no direct download is available for this
	 *         value (e.g. direct downloads are disabled for its storage, or the content is too
	 *         small to be worth a redirect). The content must then be streamed.
	 */
	URI getDirectDownloadUrl();

	/**
	 * The direct download URL of the given value.
	 *
	 * @param data
	 *        The value to deliver, may be <code>null</code>.
	 * @return The direct download URL, or <code>null</code> if the given value does not offer a
	 *         direct download, see {@link #getDirectDownloadUrl()}.
	 */
	static URI getDirectDownloadUrl(BinaryData data) {
		if (data instanceof DirectDownload directDownload) {
			return directDownload.getDirectDownloadUrl();
		}
		return null;
	}

}
