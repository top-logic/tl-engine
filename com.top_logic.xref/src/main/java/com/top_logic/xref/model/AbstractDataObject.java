/*
 * Copyright (c) 2021 Bernhard Haumacher et al. All Rights Reserved.
 */
package com.top_logic.xref.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * Base class for data object implementations.
 * 
 * <p>
 * Copied from https://github.com/msgbuf/msgbuf.
 * </p>
 * 
 * @see "https://github.com/msgbuf/msgbuf"
 */
abstract class AbstractDataObject {

	/**
	 * Writes a JSON object containing keys for all fields of this object.
	 * 
	 * <p>
	 * In contrast to {@link #writeTo(JsonWriter)}, the resulting object contains no type
	 * information. Therefore, this method must only be called directly, if the reader knows the
	 * type of the object to read from the context. For reading the data, a per-type generated
	 * <code>read[type-name]()</code> method must be called.
	 * </p>
	 *
	 * @param out
	 *        The writer to write to.
	 */
	public final void writeContent(JsonWriter out) throws java.io.IOException {
		out.beginObject();
		writeFields(out);
		out.endObject();
	}

	/**
	 * Writes all fields of this instance to the given output.
	 * 
	 * @param out
	 *        The writer to write to.
	 * @throws IOException
	 *         If writing fails.
	 */
	protected void writeFields(JsonWriter out) throws IOException {
		// No fields.
	}

	/**
	 * Reads all fields of this instance from the given input.
	 *
	 * @param in
	 *        The reader to take the input from.
	 */
	protected final void readFields(JsonReader in) throws java.io.IOException {
		while (in.hasNext()) {
			String field = in.nextName();
			readField(in, field);
		}
	}

	/**
	 * Reads the given field from the given input.
	 * 
	 * @param in
	 *        The reader to take the value from.
	 * @param field
	 *        The name of the field whose value should be read.
	 */
	protected void readField(com.top_logic.common.json.gstream.JsonReader in,
			String field) throws java.io.IOException {
		in.skipValue();
	}

	@Override
	public String toString() {
		return toJson();
	}

	/**
	 * Writes this instance to the given output.
	 * 
	 * @param out
	 *        The {@link JsonWriter} to write this instance to.
	 * 
	 * @throws IOException
	 *         if writing fails.
	 */
	public abstract void writeTo(JsonWriter out) throws IOException;

	private String toJson() {
		StringBuilder out = new StringBuilder();
		try {
			writeTo(new JsonWriter(out));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return out.toString();
	}
}
