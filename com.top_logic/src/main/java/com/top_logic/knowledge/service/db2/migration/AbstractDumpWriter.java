/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import static com.top_logic.knowledge.service.db2.migration.DumpSchemaConstants.*;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.knowledge.objects.identifier.ExtReference;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.objects.identifier.ObjectReference;
import com.top_logic.knowledge.service.db2.DBKey;
import com.top_logic.knowledge.service.db2.DBObjectKey;
import com.top_logic.knowledge.service.db2.migration.formats.ValueDumper;
import com.top_logic.knowledge.service.db2.migration.formats.ValueType;

/**
 * Abstract super class for writer dumping values to a {@link TagWriter}.
 * 
 * @see AbstractDumpReader
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AbstractDumpWriter implements Closeable, Flushable {

	public static final Map<Class<?>, ValueType> TYPES;

	static {
		HashMap<Class<?>, ValueType> map = new HashMap<>();
		map.put(Boolean.class, ValueType.BOOLEAN);
		map.put(Double.class, ValueType.DOUBLE);
		map.put(Float.class, ValueType.FLOAT);
		map.put(Byte.class, ValueType.BYTE);
		map.put(Short.class, ValueType.SHORT);
		map.put(Character.class, ValueType.CHAR);
		map.put(Integer.class, ValueType.INT);
		map.put(Long.class, ValueType.LONG);
		map.put(String.class, ValueType.STRING);
		map.put(ExtID.class, ValueType.EXT_ID);

		map.put(Date.class, ValueType.DATE);
		map.put(java.sql.Date.class, ValueType.DATE);
		map.put(Time.class, ValueType.DATE);
		map.put(Timestamp.class, ValueType.DATE);

		map.put(ExtReference.class, ValueType.EXT_REF);
		map.put(DefaultObjectKey.class, ValueType.REF);
		map.put(DBObjectKey.class, ValueType.REF);
		map.put(ObjectReference.class, ValueType.REF);
		map.put(DBKey.class, ValueType.REF);
		map.put(ObjectBranchId.class, ValueType.REF);
		TYPES = map;
	}

	/** The {@link TagWriter} to write content to */
	protected final TagWriter _out;

	/** The {@link ValueDumper} to write values */
	protected final ValueDumper _valueDumper;

	/**
	 * Creates a new {@link AbstractDumpWriter}.
	 */
	public AbstractDumpWriter(TagWriter out) {
		_out = out;
		_valueDumper = new ValueDumper(_out);
	}

	@Override
	public void flush() throws IOException {
		_out.flushBuffer();
		_out.flush();
	}

	@Override
	public void close() throws IOException {
		_out.close();
	}

	/**
	 * Dumps the given values and old values to {@link #_out}.
	 * 
	 * @param values
	 *        The new values.
	 * @param oldValues
	 *        The old values.
	 */
	protected void dumpValues(Map<String, Object> values, Map<String, Object> oldValues) throws IOException {
		for (Entry<String, Object> entry : values.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			_out.beginBeginTag(PROPERTY);
			_out.writeAttribute(NAME_ATTR, name);
			ValueType type = ValueDumper.type(value);
			_out.writeAttribute(TYPE_ATTR, type.name());
			_out.beginAttribute(VALUE_ATTR);
			_valueDumper.writeValue(type, value);
			_out.endAttribute();
			if (oldValues != null) {
				Object oldValue = oldValues.get(name);
				ValueType oldType = ValueDumper.type(oldValue);

				_out.writeAttribute(OLD_TYPE_ATTR, oldType.name());
				_out.beginAttribute(OLD_VALUE_ATTR);
				_valueDumper.writeValue(oldType, oldValue);
				_out.endAttribute();
			}
			_out.endEmptyTag();
		}
	}

	/**
	 * Dumps the given {@link ObjectBranchId} as attributes.
	 */
	protected void dumpIdAttribute(ObjectBranchId id) throws IOException {
		_out.writeAttribute(TYPE_ATTR, id.getObjectType().getName());

		_out.beginAttribute(ID_ATTR);
		writeId(id);
		_out.endAttribute();
	}

	private void writeId(ObjectBranchId id) throws IOException {
		StringServices.append(_out, id.getBranchId());
		_out.append('/');
		id.getObjectName().appendExternalForm(_out);
	}

}
