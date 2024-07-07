/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.json;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * API of an object that can serialize its property to a JSON object and read them back.
 * 
 * @param <C>
 *        The context type for de-serialization. Reading back properties happens in some context
 *        that may serve for resolving identifiers.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface JsonSerializable<C> {

	/**
	 * Creates a JSON object in the given output and writes all properties to that JSON object.
	 *
	 * @param json
	 *        The output to write to.
	 * @throws IOException
	 *         If writing fails.
	 */
	default void writeTo(JsonWriter json) throws IOException {
		json.beginObject();
		writePropertiesTo(json);
		json.endObject();
	}

	/**
	 * Creates name/value tuples for all properties of this instance in the given JSON output.
	 *
	 * @param json
	 *        The output to write to.
	 * @throws IOException
	 *         If writing fails.
	 */
	void writePropertiesTo(JsonWriter json) throws IOException;

	/**
	 * Reads a JSON object from the given reader and populates this instance with the properties of
	 * that JSON object.
	 * 
	 * <p>
	 * For reading monomorphic JSON schemata, this method is called from a static method
	 * <code>read(C context, JsonReader json)</code> defined in the type being read. This method
	 * first constructs an instance of its owning type and then calls
	 * {@link #readFrom(Object, JsonReader)} on this instance before returning it.
	 * </p>
	 *
	 * @param context
	 *        Some context that may serve for resolving identifiers.
	 * @param json
	 *        The output to write to.
	 * @throws IOException
	 *         If reading fails.
	 */
	default void readFrom(C context, JsonReader json) throws IOException {
		json.beginObject();
		while (json.hasNext()) {
			readPropertyFrom(context, json, json.nextName());
		}
		json.endObject();
	}

	/**
	 * Reads the property value for the property with the given name and assigns the read value to
	 * this instance.
	 * 
	 * <p>
	 * The default implementation always throws an exception that signals that the given property is
	 * not understood.
	 * </p>
	 *
	 * @param context
	 *        Some context that may serve for resolving identifiers.
	 * @param json
	 *        The output to write to.
	 * @param name
	 *        The name of the property to read and assign.
	 * @throws IOException
	 *         If reading the property value fails.
	 * @throws IllegalArgumentException
	 *         If the property with the given name is not understood.
	 */
	default void readPropertyFrom(C context, JsonReader json, String name) throws IOException {
		throw new IllegalArgumentException("No such property '" + name + "' in '" + getClass().getName() + "'.");
	}

	/**
	 * Utility for writing a JSON array consisting of the given iteration of
	 * {@link JsonSerializable} instances.
	 *
	 * @param json
	 *        The output to write to.
	 * @param instances
	 *        The instances to write as contents of the created JSON array.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeArray(JsonWriter json, Iterable<? extends JsonSerializable<?>> instances)
			throws IOException {
		json.beginArray();
		for (JsonSerializable<?> option : instances) {
			option.writeTo(json);
		}
		json.endArray();
	}

}
