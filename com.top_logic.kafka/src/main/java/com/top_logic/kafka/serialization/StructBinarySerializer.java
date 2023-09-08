/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.serialization;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Schema.Type;
import org.apache.kafka.connect.data.Struct;

/**
 * A {@link Serializer} for implementation {@link Struct}s which supports only
 * the following primitive types:
 * 
 * <ul>
 * <li>{@link Type#STRING} - see {@link DataInput}</li>
 * <li>{@link Type#BOOLEAN} - 1 byte of data</li>
 * <li>{@link Type#FLOAT64} - 8 bytes of data</li>
 * <li>{@link Type#FLOAT32} - 4 bytes of data</li>
 * <li>{@link Type#INT64} - 8 bytes of data</li>
 * <li>{@link Type#INT32} - 4 bytes of data</li>
 * <li>{@link Type#INT16} - 2 bytes of data</li>
 * <li>{@link Type#INT8} - 1 byte of data</li>
 * </ul>
 * 
 * <p>
 * The record's header contains the {@link Schema#name()} and
 * {@link Schema#version()}.
 * </p>
 * 
 * <p>
 * Each {@link Field} of a {@link Struct} is prefixed with one byte flag,
 * stating whether the value is set (1) or not (0). If the value is not set, the
 * {@link Deserializer} implementation must skip to the next entry.
 * </p>
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class StructBinarySerializer extends TLSerializer<Struct> {
	
	@Override
	public void close() {
		// does nothing
	}

	@Override
	public byte[] serialize(final String topic, final Struct struct) {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final DataOutputStream stream = new DataOutputStream(bytes);
		
		// write schema name and version first
		final Schema schema = struct.schema();
		writeString(schema.name(), stream);
		writeInteger(schema.version(), stream);
		
		// write the struct contents
		for (final Field field : schema.fields()) {
			
			// do not serialize the default value to save bandwidth
			final Object value = struct.getWithoutDefault(field.name());
			
			switch(field.schema().type()) {
			case STRING:
				writeString((String) value, stream);
				break;
			case BOOLEAN:
				writeBoolean((Boolean) value, stream);
				break;
			case FLOAT64:
				writeDouble((Double) value, stream);
				break;
			case FLOAT32:
				writeFloat((Float) value, stream);
				break;
			case INT64:
				writeLong((Long) value, stream);
				break;
			case INT32:
				writeInteger((Integer) value, stream);
				break;
			case INT16:
				writeShort((Short) value, stream);
				break;
			case INT8:
				writeByte((Byte) value, stream);
				break;
			default:
				// unsupported types are simply ignore without error messages
				break;
			}
		}
		
		return bytes.toByteArray();
	}

	/**
	 * Write the given {@link String} value to the given output stream.
	 * 
	 * @param value
	 *            the {@link String} value to be written or {@code null}
	 * @param stream
	 *            {@link DataOutputStream} to write the value to
	 */
	protected void writeString(final String value, final DataOutputStream stream) {
		final boolean isSet = value != null;
		
		try {
			stream.writeBoolean(isSet);
			if (isSet) {
				stream.writeUTF(value);
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Write the given {@link Boolean} value to the given output stream.
	 * 
	 * @param value
	 *            the {@link Boolean} value to be written or {@code null}
	 * @param stream
	 *            {@link DataOutputStream} to write the value to
	 */
	protected void writeBoolean(final Boolean value, final DataOutputStream stream) {
		final boolean isSet = value != null;
		
		try {
			stream.writeBoolean(isSet);
			if (isSet) {
				stream.writeBoolean(value);
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Write the given {@link Double} value to the given output stream.
	 * 
	 * @param value
	 *            the {@link Double} value to be written or {@code null}
	 * @param stream
	 *            {@link DataOutputStream} to write the value to
	 */
	protected void writeDouble(final Double value, final DataOutputStream stream) {
		final boolean isSet = value != null;
		
		try {
			stream.writeBoolean(isSet);
			if (isSet) {
				stream.writeDouble(value);
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Write the given {@link Float} value to the given output stream.
	 * 
	 * @param value
	 *            the {@link Float} value to be written or {@code null}
	 * @param stream
	 *            {@link DataOutputStream} to write the value to
	 */
	protected void writeFloat(final Float value, final DataOutputStream stream) {
		final boolean isSet = value != null;
		
		try {
			stream.writeBoolean(isSet);
			if (isSet) {
				stream.writeFloat(value);
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Write the given {@link Long} value to the given output stream.
	 * 
	 * @param value
	 *            the {@link Long} value to be written or {@code null}
	 * @param stream
	 *            {@link DataOutputStream} to write the value to
	 */
	protected void writeLong(final Long value, final DataOutputStream stream) {
		final boolean isSet = value != null;
		
		try {
			stream.writeBoolean(isSet);
			if (isSet) {
				stream.writeLong(value);
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Write the given {@link Integer} value to the given output stream.
	 * 
	 * @param value
	 *            the {@link Integer} value to be written or {@code null}
	 * @param stream
	 *            {@link DataOutputStream} to write the value to
	 */
	protected void writeInteger(final Integer value, final DataOutputStream stream) {
		final boolean isSet = value != null;
		
		try {
			stream.writeBoolean(isSet);
			if (isSet) {
				stream.writeInt(value);
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Write the given {@link Short} value to the given output stream.
	 * 
	 * @param value
	 *            the {@link Short} value to be written or {@code null}
	 * @param stream
	 *            {@link DataOutputStream} to write the value to
	 */
	protected void writeShort(final Short value, final DataOutputStream stream) {
		final boolean isSet = value != null;
		
		try {
			stream.writeBoolean(isSet);
			if (isSet) {
				stream.writeShort(value);
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Write the given {@link Byte} value to the given output stream.
	 * 
	 * @param value
	 *            the {@link Byte} value to be written or {@code null}
	 * @param stream
	 *            {@link DataOutputStream} to write the value to
	 */
	protected void writeByte(final Byte value, final DataOutputStream stream) {
		final boolean isSet = value != null;
		
		try {
			stream.writeBoolean(isSet);
			if (isSet) {
				stream.writeByte(value);
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
