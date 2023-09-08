/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * An entry of a {@link ZipArchive}.
 */
public interface ZipEntrySource {

	/**
	 * Writs the entry to the given {@link ZipOutputStream}.
	 */
	void deliverTo(ZipOutputStream zip) throws IOException;

}
