/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.update;

import java.io.IOException;

import com.top_logic.basic.shared.io.StringR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * Utility methods for working with {@link Change} and {@link Changes}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ChangeIO {

	/**
	 * Writes the given {@link Changes} as JSON.
	 * <p>
	 * {@link IOException}s are wrapped into {@link RuntimeException}s.
	 * </p>
	 * 
	 * @param changes
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static String writeChanges(Changes changes) {
		StringBuilder buffer = new StringBuilder();
		try (JsonWriter jsonWriter = new JsonWriter(buffer)) {
			changes.writeTo(jsonWriter);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to write update. Cause: " + ex.getMessage(), ex);
		}
		return buffer.toString();
	}

	/**
	 * Reads the {@link Changes} from the given JSON.
	 * <p>
	 * {@link IOException}s are wrapped into {@link RuntimeException}s.
	 * </p>
	 * 
	 * @param changes
	 *        Is not allowed to be null. The JSON encoded {@link Changes}.
	 * @return Never null.
	 */
	public static Changes readChanges(String changes) {
		try (JsonReader reader = new JsonReader(new StringR(changes))) {
			return Changes.loadChanges(reader);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to read update. Cause: " + ex.getMessage(), ex);
		}
	}

}
