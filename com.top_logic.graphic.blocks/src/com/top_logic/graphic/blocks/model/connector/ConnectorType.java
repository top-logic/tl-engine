/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.connector;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.block.Connector;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * Description of the outline of a {@link Connector}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConnectorType {

	private final String _name;

	/**
	 * Creates a {@link ConnectorType}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 */
	public ConnectorType(String name) {
		_name = name;
	}

	/**
	 * The ID of this {@link ConnectorType}.
	 * 
	 * @see BlockSchema#getConnectorType(String)
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Whether a {@link Connector} of this {@link ConnectorType} can (semantically) connect to a
	 * {@link Connector} of the given {@link ConnectorType}.
	 */
	public boolean canConnectTo(ConnectorType other) {
		return this == other;
	}

	/**
	 * Draws the outline of a {@link Connector} of this {@link ConnectorType}.
	 *
	 * @param out
	 *        The {@link SvgWriter} to draw to.
	 * @param kind
	 *        The position of the {@link Connector} using this {@link ConnectorType}.
	 */
	public abstract void outline(SvgWriter out, ConnectorKind kind);

	/**
	 * Height of the connector plug.
	 */
	public abstract double getHeight();

	/**
	 * Writes a reference to this {@link ConnectorType} as value to the given {@link JsonWriter}.
	 */
	public void writeTo(JsonWriter json) throws IOException {
		json.value(getName());
	}

	/**
	 * Utility for negating the given value, if right-to-left rendering is used.
	 */
	protected static double flip(boolean rtl, double value) {
		return rtl ? -value : value;
	}

	/**
	 * Reads and resolves a reference to a {@link ConnectorType} in the given {@link BlockSchema}.
	 */
	public static ConnectorType read(BlockSchema blockSchema, JsonReader json) throws IOException {
		return blockSchema.getConnectorType(json.nextString());
	}

}
