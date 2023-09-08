/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.json;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;

/**
 * API for serializing an object in JSON format.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface JsonSerializable {

	/**
	 * Writes a JSON object literal representing this instance to the given writer.
	 * 
	 * @param writer
	 *        The JSON output writer to write to.
	 * @throws IOException
	 *         If writing fails.
	 */
	void writeTo(JsonWriter writer) throws IOException;

}
