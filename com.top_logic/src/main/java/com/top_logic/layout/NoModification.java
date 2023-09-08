/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * {@link DownloadModification} that does not modify the given data.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class NoModification implements DownloadModification {

	/** Singleton {@link NoModification} instance. */
	public static final NoModification INSTANCE = new NoModification();

	private NoModification() {
		// singleton instance
	}

	@Override
	public BinaryData modifyDownload(BinaryData data, boolean showInline) {
		return data;
	}

}

