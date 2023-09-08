/*
 * Copyright (c) 2021 Bernhard Haumacher et al. All Rights Reserved.
 */
package com.top_logic.xref.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonToken;

/**
 * Utilities for reading and writing JSON.
 *
 * <p>
 * Copied from https://github.com/msgbuf/msgbuf.
 * </p>
 * 
 * @see "https://github.com/msgbuf/msgbuf"
 */
class JsonUtil {

	/**
	 * Reads a string or a <code>null</code> value from the given reader.
	 */
	public static String nextStringOptional(JsonReader in) throws IOException {
		if (in.peek() == JsonToken.NULL) {
			in.nextNull();
			return null;
		}

		return in.nextString();
	}

}
