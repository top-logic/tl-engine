/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.serialization;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

/**
 * A {@link Deserializer} implementation for {@link Struct}s which were
 * serialized by {@link StructBinarySerializer}s.
 * 
 * @author <a href=mailto:wta@top-logic.com>wta</a>
 */
public class StructBinaryDeserializer extends TLDeserializer<Struct> {

	/**
	 * The internal registry of {@link Schema} by name and version.
	 */
	private final Map<String, Map<Integer, Schema>> _schemata = new LinkedHashMap<>();
	
	/**
	 * Add support for the given schema.
	 * 
	 * @param schema
	 *            the {@link Schema} to support
	 */
	public void addSchema(final Schema schema) {
		final String name = schema.name();
		final Integer version = schema.version();
		
		synchronized(_schemata) {
			Map<Integer, Schema> map = _schemata.get(name);
			
			if(map == null) {
				map = new LinkedHashMap<>();
				_schemata.put(name, map);
			}
			
			map.put(version, schema);
		}
	}

	/**
	 * Remove support for the given schema.
	 * 
	 * @param schema
	 *            the {@link Schema} to remove support for
	 */
	public void removeSchema(final Schema schema) {
		synchronized(_schemata) {
			final Map<Integer, Schema> map = _schemata.get(schema.name());
			
			if(map != null) {
				map.remove(schema.version());
			}
		}
	}
	
	@Override
	public void close() {
		// does nothing
	}

	@Override
	public Struct deserialize(final String topic, final byte[] data) {
		final ByteArrayInputStream bytes = new ByteArrayInputStream(data);
		final DataInputStream stream = new DataInputStream(bytes);
		
		// read schema name and version first
		final String name = readString(stream);
		final Integer version = readInteger(stream);
		final Schema schema = getSchema(name, version);
		
		// schema is not supported
		if(schema == null) {
			return null;
		}
		
		// read data and fill the output struct with values
		final Struct struct = new Struct(schema);
		for (final Field field : schema.fields()) {
			final Object value;
			
			switch(field.schema().type()) {
			case STRING:
				value = readString(stream);
				break;
			case BOOLEAN:
				value = readBoolean(stream);
				break;
			case FLOAT64:
				value = readDouble(stream);
				break;
			case FLOAT32:
				value = readFloat(stream);
				break;
			case INT64:
				value = readLong(stream);
				break;
			case INT32:
				value = readInteger(stream);
				break;
			case INT16:
				value = readShort(stream);
				break;
			case INT8:
				value = readByte(stream);
				break;
			default:
				// unsupported types are simply ignore without error messages
				value = null;
				break;
			}

			// accept only non-null values
			if(value != null) {
				struct.put(field, value);
			}
		}
		
		return struct;
	}

	/**
	 * Read a {@link String} value from the given input stream.
	 * 
	 * @param stream
	 *            {@link DataInputStream} to read the value from
	 * @return the read {@link String} value or {@code null}
	 */
	protected String readString(final DataInputStream stream) {
		try {
			if(isSet(stream)) {
				return stream.readUTF();
			} else {
				return null;
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Read a {@link Boolean} value from the given input stream.
	 * 
	 * @param stream
	 *            {@link DataInputStream} to read the value from
	 * @return the read {@link Boolean} value or {@code null}
	 */
	protected Boolean readBoolean(final DataInputStream stream) {
		try {
			if(isSet(stream)) {
				return stream.readBoolean();
			} else {
				return null;
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read a {@link Double} value from the given input stream.
	 * 
	 * @param stream
	 *            {@link DataInputStream} to read the value from
	 * @return the read {@link Double} value or {@code null}
	 */
	protected Double readDouble(final DataInputStream stream) {
		try {
			if(isSet(stream)) {
				return stream.readDouble();
			} else {
				return null;
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read a {@link Float} value from the given input stream.
	 * 
	 * @param stream
	 *            {@link DataInputStream} to read the value from
	 * @return the read {@link Float} value or {@code null}
	 */
	protected Float readFloat(final DataInputStream stream) {
		try {
			if(isSet(stream)) {
				return stream.readFloat();
			} else {
				return null;
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read a {@link Long} value from the given input stream.
	 * 
	 * @param stream
	 *            {@link DataInputStream} to read the value from
	 * @return the read {@link Long} value or {@code null}
	 */
	protected Long readLong(final DataInputStream stream) {
		try {
			if(isSet(stream)) {
				return stream.readLong();
			} else {
				return null;
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read an {@link Integer} value from the given input stream.
	 * 
	 * @param stream
	 *            {@link DataInputStream} to read the value from
	 * @return the read {@link Integer} value or {@code null}
	 */
	protected Integer readInteger(final DataInputStream stream) {
		try {
			if(isSet(stream)) {
				return stream.readInt();
			} else {
				return null;
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read a {@link Short} value from the given input stream.
	 * 
	 * @param stream
	 *            {@link DataInputStream} to read the value from
	 * @return the read {@link Short} value or {@code null}
	 */
	protected Short readShort(final DataInputStream stream) {
		try {
			if(isSet(stream)) {
				return stream.readShort();
			} else {
				return null;
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read a {@link Byte} value from the given input stream.
	 * 
	 * @param stream
	 *            {@link DataInputStream} to read the value from
	 * @return the read {@link Byte} value or {@code null}
	 */
	protected Byte readByte(final DataInputStream stream) {
		try {
			if(isSet(stream)) {
				return stream.readByte();
			} else {
				return null;
			}
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * @param stream
	 *            the {@link DataInputStream} to read if a field's value is set
	 *            in the given stream or not
	 * @return {@code true} if the field's value is set, {@code false} otherwise
	 * @throws IOException
	 *             if an error occurred when reading from the given stream
	 */
	protected boolean isSet(final DataInputStream stream) throws IOException {
		return stream.readBoolean();
	}
	
	/**
	 * @param name
	 *            the name {@link String} of the schema to be resolved
	 * @param version
	 *            the version {@link Integer} to resolve the schema in
	 * @return the resolved {@link Schema} or {@code null} if the schema is not
	 *         supported
	 */
	protected Schema getSchema(final String name, final Integer version) {
		synchronized (_schemata) {
			final Map<Integer, Schema> map = _schemata.get(name);
			
			if(map != null) {
				return map.get(version);
			} else {
				return null;
			}
		}
	}
}
