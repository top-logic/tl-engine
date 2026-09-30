/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.formats;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Date;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.ExtIDFormat;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.knowledge.objects.identifier.ExtReference;
import com.top_logic.knowledge.objects.identifier.ExtReferenceFormat;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.migration.AbstractDumpWriter;

/**
 * Helper class to dump values of a given {@link ValueType}
 * 
 * @see ValueParser
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ValueDumper {

	private final Writer _out;

	/**
	 * Creates a new ValueDumper.
	 * 
	 * @param out
	 *        The {@link Writer} to dump values to.
	 */
	public ValueDumper(Writer out) {
		_out = out;
	}

	/**
	 * Writes the given value of the given type to the given writer in dump value format.
	 * 
	 * @param type
	 *        The type of the given value, see {@link #type(Object)}.
	 * @param value
	 *        The value to serialize.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 */
	public void writeValue(ValueType type, Object value) throws IOException {
		switch (type) {
			case NULL:
				return;
			case BOOLEAN:
			case DOUBLE:
			case FLOAT:
			case INT:
			case SHORT:
			case BYTE:
			case CHAR:
			case LONG:
			case STRING:
			case CONFIG:
				_out.append(value.toString());
				return;
			case DATE:
				Date date = (Date) value;
				_out.append(XmlDateTimeFormat.INSTANCE.format(date));
				return;
			case INSTANCE:
				ConfiguredInstance<?> instance = (ConfiguredInstance<?>) value;
				_out.append(instance.getConfig().toString());
				return;
			case ENUM:
				Enum<?> enumConst = (Enum<?>) value;
				_out.append(enumConst.getDeclaringClass().getName() + "#" + enumConst.name());
				return;
			case TLID:
				_out.append(IdentifierUtil.toExternalForm((TLID) value));
				return;
			case REF:
				if (value instanceof ObjectBranchId) {
					ObjectBranchId id = (ObjectBranchId) value;
					writeRef(id);
				} else if (value instanceof ObjectKey) {
					ObjectKey id = (ObjectKey) value;
					writeRef(id);
				} else if (value instanceof IdentifiedObject) {
					ObjectKey id = ((IdentifiedObject) value).tId();
					writeRef(id);
				}
				return;
			case EXT_REF:
				writeRef((ExtReference) value);
				return;
			case EXT_ID:
				writeExtId((ExtID) value);
				return;
			case BINARY:
				_out.append(new String(Base64.encodeBase64((byte[]) value)));
				return;
			case BINARY_DATA:
				writeBinaryData(BinaryData.cast(value));
				return;
			case SINGLETON:
				_out.append(value.getClass().getName());
				return;
		}
		throw new UnreachableAssertion("No such type: " + type);
	}

	private void writeExtId(ExtID id) throws IOException {
		_out.append(ExtIDFormat.INSTANCE.format(id));
	}

	private void writeRef(ObjectKey id) throws IOException {
		writeRef(id.getBranchContext(), id.getHistoryContext(), id.getObjectType(), id.getObjectName());
	}

	private void writeRef(long branch, long rev, MetaObject type, TLID name) throws IOException {
		writeType(type);
		writeBranch(branch);
		name.appendExternalForm(_out);
		if (rev != Revision.CURRENT_REV) {
			_out.append('#');
			StringServices.append(_out, rev);
		}
	}

	private void writeType(MetaObject type) throws IOException {
		_out.append(type.getName());
		_out.append(':');
	}

	private void writeBranch(long branch) throws IOException {
		StringServices.append(_out, branch);
		_out.append('/');
	}

	private void writeRef(ObjectBranchId id) throws IOException {
		writeRef(id.getBranchId(), Revision.CURRENT_REV, id.getObjectType(), id.getObjectName());
	}

	private void writeRef(ExtReference id) throws IOException {
		_out.append(ExtReferenceFormat.INSTANCE.format(id));
	}

	/**
	 * Writes a {@link BinaryData} to a {@link Writer} whose content can later be parsed using
	 * {@link ValueParser#parseValue(ValueType, String)}.
	 * @param value
	 *        The {@link BinaryData} to write.
	 */
	private void writeBinaryData(BinaryData value) throws IOException {
		String contentType = value.getContentType();

		String fileName = value.getName();
		if (!StringServices.isEmpty(fileName)) {
			// Encode file name as mime type parameter.
			try {
				MimeType mimeType = new MimeType(contentType);
				mimeType.setParameter(ValueParser.BINARY_DATA_FILE_NAME_PARAMETER, fileName);
				contentType = mimeType.toString();
			} catch (MimeTypeParseException ex) {
				// Do not try to add the file name to an invalid content type, since parsing would
				// fail anyway.
				Logger.error("Invalid content type in binary content: " + contentType, ex);
			}
		}

		_out.write(contentType.replaceAll("" + ValueParser.BINARY_DATA_TYPE_SEPARATOR,
			"" + ValueParser.BINARY_DATA_TYPE_SEPARATOR + ValueParser.BINARY_DATA_TYPE_SEPARATOR));
		_out.write(ValueParser.BINARY_DATA_TYPE_SEPARATOR);
		try (InputStream in = value.getStream()) {
			Base64InputStream encodedStream = new Base64InputStream(in, true);
			InputStreamReader reader = new InputStreamReader(encodedStream, StringServices.CHARSET_UTF_8);
			StreamUtilities.copyReaderWriterContents(reader, _out);
		}
	}

	/**
	 * Analyses the type of the given value.
	 * 
	 * @param value
	 *        The value to analyse.
	 * @return The value type required for {@link #writeValue(ValueType, Object)}.
	 */
	public static ValueType type(Object value) {
		if (value == null) {
			return ValueType.NULL;
		}
	
		// Short-cut for easy cases.
		ValueType result = AbstractDumpWriter.TYPES.get(value.getClass());
		if (result != null) {
			return result;
		}
	
		if (value instanceof Date) {
			return ValueType.DATE;
		}
		if (value instanceof ObjectKey) {
			return ValueType.REF;
		}
		if (value instanceof ObjectBranchId) {
			return ValueType.REF;
		}
		if (value instanceof IdentifiedObject) {
			return ValueType.REF;
		}
		if (value instanceof ExtReference) {
			return ValueType.EXT_REF;
		}
		if (value instanceof ConfigurationItem) {
			return ValueType.CONFIG;
		}
		if (value instanceof ConfiguredInstance<?>) {
			return ValueType.INSTANCE;
		}
		if (value instanceof Enum<?>) {
			return ValueType.ENUM;
		}
		if (value instanceof TLID) {
			return ValueType.TLID;
		}
		if (value instanceof ExtID) {
			return ValueType.EXT_ID;
		}
		if (value.getClass().isArray() && value.getClass().getComponentType() == byte.class) {
			return ValueType.BINARY;
		}
		if (value instanceof BinaryData) {
			return ValueType.BINARY_DATA;
		}
	
		return ValueType.SINGLETON;
	}

}

